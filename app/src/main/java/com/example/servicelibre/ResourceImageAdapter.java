package com.example.servicelibre;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arcsolu.sopda.entity.Menu;
import com.arcsolu.sopda.entity.Order;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * This class is an adapter that provides images from a fixed set of resource
 * ids. Bitmaps and ImageViews are kept as weak references so that they can be
 * cleared by garbage collection when not needed.
 * 
 */
public class ResourceImageAdapter extends AbstractCoverFlowImageAdapter {

	/** The Constant TAG. */
	private static final String TAG = ResourceImageAdapter.class
			.getSimpleName();

	/** The bitmap map. */
	private final Map<Integer, WeakReference<Bitmap>> bitmapMap = new HashMap<Integer, WeakReference<Bitmap>>();

	private final Context context;

	/**
	 * Creates the adapter with default set of resource images.
	 * 
	 * @param context
	 *            context
	 */
	public ResourceImageAdapter(final Context context, List<Menu> list,
			Order order) {
		super(order, list);
		this.context = context;

	}

	@Override
	public synchronized int getCount() {
		return list.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.polidea.coverflow.AbstractCoverFlowImageAdapter#createBitmap(int)
	 */
	@Override
	protected Bitmap createBitmap(final int position) {
		Log.v(TAG, "creating item " + position);
		final Bitmap bitmap = BitmapFactory.decodeByteArray(
				list.get(position).Pic, 0, list.get(position).Pic.length);
		bitmapMap.put(position, new WeakReference<Bitmap>(bitmap));
		return bitmap;
	}
}