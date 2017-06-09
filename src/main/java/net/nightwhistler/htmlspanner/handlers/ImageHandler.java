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

import net.nightwhistler.htmlspanner.SpanStack;
import net.nightwhistler.htmlspanner.TagNodeHandler;
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

	Integer widthFromStyles;
	Integer heightFromStyles;

	@Override
	public void beforeChildren(TagNode node, SpannableStringBuilder builder, SpanStack spanStack) {
		super.beforeChildren(node, builder, spanStack);
		Style useStyle = spanStack.getStyle( node, new Style() );

		if(useStyle.getWidth() != null ) {
			StyleValue styleValue = useStyle.getWidth();
			if (styleValue.getUnit() == StyleValue.Unit.PX) {
				if (styleValue.getIntValue() > 0 ) {
					widthFromStyles = styleValue.getIntValue();
				}
			}
		}

		if(useStyle.getHeight() != null) {
			StyleValue styleValue = useStyle.getHeight();
			if (styleValue.getUnit() == StyleValue.Unit.PX) {
				if (styleValue.getIntValue() > 0 ) {
					heightFromStyles = styleValue.getIntValue();
				}
			}
		}
	}

	@Override
	public void handleTagNode(TagNode node, SpannableStringBuilder builder, int start, int end, SpanStack stack) {
		String src = node.getAttributeByName("src");
		String width = node.getAttributeByName("width");
		String height = node.getAttributeByName("height");
		builder.append("￼");
		Bitmap bitmap = this.loadBitmap(src);
		if(bitmap != null) {
			BitmapDrawable drawable = new BitmapDrawable(bitmap);
			drawable.setBounds(0, 0,
					widthFromStyles != null ? widthFromStyles : ParseUtils.parseIntegerSafe(width, bitmap.getWidth() - 1),
					heightFromStyles != null ? heightFromStyles : ParseUtils.parseIntegerSafe(height, bitmap.getHeight() - 1));
			stack.pushSpan(new ImageSpan(drawable), start, builder.length());
		}
	}

	/**
	 * Loads a Bitmap from the given url.
	 * 
	 * @param url
	 * @return a Bitmap, or null if it could not be loaded.
	 */
	protected Bitmap loadBitmap(String url) {
		try {
			return BitmapFactory.decodeStream(new URL(url).openStream());
		} catch (IOException io) {
			return null;
		}
	}
}
