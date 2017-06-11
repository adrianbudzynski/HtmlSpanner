/*
 * Copyright (C) 2011 Alex Kuiper <http://www.nightwhistler.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.nightwhistler.htmlspanner.handlers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.LruCache;

import net.nightwhistler.htmlspanner.SpanStack;
import net.nightwhistler.htmlspanner.TagNodeHandler;
import net.nightwhistler.htmlspanner.css.CSSCompiler;
import net.nightwhistler.htmlspanner.style.Style;
import net.nightwhistler.htmlspanner.style.StyleValue;
import net.nightwhistler.htmlspanner.utils.ParseUtils;

import org.htmlcleaner.TagNode;

import java.io.IOException;
import java.net.URL;

/**
 * Handles image tags.
 * 
 * The default implementation tries to load images through a URL.openStream(),
 * override loadBitmap() to implement your own loading.
 * 
 * @author Alex Kuiper
 * 
 */
public class ImageHandler extends TagNodeHandler {

	private LruCache<String, Bitmap> mMemoryCache;
	private final float scale;

	public ImageHandler(float scale) {
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		final int cacheSize = maxMemory / 8;
		this.scale = scale;
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getByteCount() / 1024;
			}
		};
	}

	@Override
	public void handleTagNode(TagNode node, SpannableStringBuilder builder, int start, int end, SpanStack stack) {
		String styleAttr = node.getAttributeByName("style");
		Style useStyle = this.parseStyleFromAttribute(new Style(), styleAttr);

		StyleValue widthFromStyles = useStyle.getWidth();
		StyleValue heightFromStyles = useStyle.getHeight();

		String src = node.getAttributeByName("src");
		String width = node.getAttributeByName("width");
		String height = node.getAttributeByName("height");
		builder.append("￼");
		Bitmap bitmap = this.loadBitmap(src);
		if(bitmap != null) {
			BitmapDrawable drawable = new BitmapDrawable(bitmap);
			int imageWidth = (int) ((widthFromStyles != null
					? widthFromStyles.getIntValue()
						: ParseUtils.parseIntegerSafe(width, bitmap.getWidth() - 1)) * scale);
			int imageHeight = (int) ((heightFromStyles != null
					? heightFromStyles.getIntValue()
						: ParseUtils.parseIntegerSafe(height, bitmap.getHeight() - 1)) * scale);
			drawable.setBounds(0, 0, imageWidth, imageHeight);
			stack.pushSpan(new ImageSpan(drawable), start, builder.length());
		}
	}

	private Style parseStyleFromAttribute(Style baseStyle, String attribute) {
		if(attribute == null) {
			return baseStyle;
		}
		Style style = baseStyle;
		String[] pairs = attribute.split(";");
		String[] arr$ = pairs;
		int len$ = pairs.length;

		for(int i$ = 0; i$ < len$; ++i$) {
			String pair = arr$[i$];
			String[] keyVal = pair.split(":");
			if(keyVal.length != 2) {
				Log.e("StyleAttributeHandler", "Could not parse attribute: " + attribute);
				return baseStyle;
			}

			String key = keyVal[0].toLowerCase().trim();
			String value = keyVal[1].toLowerCase().trim();
			CSSCompiler.StyleUpdater updater = CSSCompiler.getStyleUpdater(key, value);
			if(updater != null) {
				style = updater.updateStyle(style, this.getSpanner());
			}
		}

		return style;
	}

	protected Bitmap loadBitmap(String url) {
		Bitmap bitmap = getBitmapFromMemCache(url);
		if(bitmap == null) {
			bitmap = loadBitmapFromNetwork(url);
			addBitmapToMemoryCache(url, bitmap);
			return bitmap;
		} else {
			return bitmap;
		}
	}

	protected Bitmap loadBitmapFromNetwork(String url) {
		try {
			return BitmapFactory.decodeStream(new URL(url).openStream());
		} catch (IOException io) {
			return null;
		}
	}

	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	public Bitmap getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}

}
