package com.flipkart.layoutengine.builder;

import android.content.Context;
import android.support.annotation.Nullable;

import com.flipkart.layoutengine.ParserContext;
import com.flipkart.layoutengine.exceptions.InvalidDataPathException;
import com.flipkart.layoutengine.exceptions.JsonNullException;
import com.flipkart.layoutengine.exceptions.NoSuchDataPathException;
import com.flipkart.layoutengine.provider.JsonProvider;
import com.flipkart.layoutengine.provider.Provider;
import com.flipkart.layoutengine.toolbox.Utils;
import com.flipkart.layoutengine.view.ProteusView;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A layout builder which can parse @data and @view blocks before passing it on to
 * {@link SimpleLayoutBuilder}
 */
public class DataAndViewParsingLayoutBuilder extends DataParsingLayoutBuilder {

    private Provider viewProvider;
    private static final Logger logger = LoggerFactory.getLogger(DataAndViewParsingLayoutBuilder.class);

    protected DataAndViewParsingLayoutBuilder(Context context, JsonObject viewProvider, @Nullable IdGenerator idGenerator) {
        super(context, idGenerator);
        this.viewProvider = new JsonProvider(viewProvider);
    }

    @Override
    protected ProteusView onUnknownViewEncountered(ParserContext context, String viewType,
                                                   ProteusView parent, JsonObject layout,
                                                   int childIndex) {
        JsonElement viewElement = null;
        if (viewProvider != null) {
            try {
                viewElement = viewProvider.getObject(viewType, childIndex);
            } catch (InvalidDataPathException | NoSuchDataPathException | JsonNullException e) {
                logger.error("onUnknownViewEncountered " + e.getMessage());
            }
        }
        if (viewElement != null) {
            JsonObject viewLayoutObject = viewElement.getAsJsonObject();
            ProteusView createdView = buildImpl(context, parent, viewLayoutObject, childIndex, parent.getStyles());
            ParserContext newParserContext = getNewParserContext(context, viewLayoutObject, childIndex);
            onViewBuiltFromViewProvider(createdView, viewType, newParserContext, viewLayoutObject, parent, childIndex);
            return createdView;
        }
        return super.onUnknownViewEncountered(context, viewType, parent, layout, childIndex);
    }

    public void updateLayoutProvider(JsonObject newViewProvider) {
        if (viewProvider != null && viewProvider.getData() != null) {
            JsonElement viewProviderData = Utils.addElements(viewProvider.getData().getAsJsonObject(), newViewProvider, true);
            viewProvider.setData(viewProviderData);
        } else {
            viewProvider = new JsonProvider(newViewProvider);
        }
    }

    private void onViewBuiltFromViewProvider(ProteusView createdView, String viewType,
                                             ParserContext parserContext, JsonObject viewLayoutObject,
                                             ProteusView parent, int childIndex) {
        if (listener != null) {
            listener.onViewBuiltFromViewProvider(createdView, viewType, parserContext, viewLayoutObject,
                    parent, childIndex);
        }
    }
}
