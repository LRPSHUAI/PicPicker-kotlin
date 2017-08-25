package com.lrpshuai.picpicker.weight.photoviews;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class PhotoViewPager extends ViewPager {

	/**
	 * maximum z distance to translate child view
	 */
	final static int DEFAULT_OVERSCROLL_TRANSLATION = 150;

	/**
	 * duration of overscroll animation in ms
	 */
	@SuppressWarnings("unused")
	private final static String DEBUG_TAG = ViewPager.class.getSimpleName();
	private final static int INVALID_POINTER_ID = -1;

	private OnPageChangeListener mScrollListener;
	private float mLastMotionX;
	private int mActivePointerId;
	private float mScrollPositionOffset;
	final private int mTouchSlop;
	private MyDirectListener myDirectListener;

	public void setMyDirectListener(MyDirectListener myDirectListener) {
		this.myDirectListener = myDirectListener;
	}

	public PhotoViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		setStaticTransformationsEnabled(true);
		final ViewConfiguration configuration = ViewConfiguration.get(context);
		mTouchSlop = ViewConfigurationCompat
				.getScaledPagingTouchSlop(configuration);
		super.setOnPageChangeListener(new MyOnPageChangeListener());
	}

	@Override
	public void setOnPageChangeListener(OnPageChangeListener listener) {
		mScrollListener = listener;
	};

	private class MyOnPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			if (mScrollListener != null) {
				mScrollListener.onPageScrolled(position, positionOffset,
						positionOffsetPixels);
			}
			mScrollPositionOffset = positionOffset;
		}

		@Override
		public void onPageSelected(int position) {

			if (mScrollListener != null) {
				mScrollListener.onPageSelected(position);
			}
		}

		@Override
		public void onPageScrollStateChanged(final int state) {

			if (mScrollListener != null) {
				mScrollListener.onPageScrollStateChanged(state);
			}
			if (state == SCROLL_STATE_IDLE) {
				mScrollPositionOffset = 0;
			}
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(isCanScroll){
			try {
				final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
				switch (action) {
					case MotionEvent.ACTION_DOWN: {
						mLastMotionX = ev.getX();
						mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
						break;
					}
					case MotionEventCompat.ACTION_POINTER_DOWN: {
						final int index = MotionEventCompat.getActionIndex(ev);
						final float x = MotionEventCompat.getX(ev, index);
						mLastMotionX = x;
						mActivePointerId = MotionEventCompat.getPointerId(ev, index);
						break;
					}
				}
				return super.onInterceptTouchEvent(ev);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				return false;
			} catch (ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
				return false;
			}
		}else{
			return false;
		}

	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		try {
			return super.dispatchTouchEvent(ev);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			return false;
		}
	}
	private boolean isCanScroll = true;
	public void setCanScroll(boolean isCanScroll){
		this.isCanScroll = isCanScroll;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if(isCanScroll){
			final int action = ev.getAction();
			switch (action) {
				case MotionEvent.ACTION_DOWN: {
					mLastMotionX = ev.getX();
					mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
					break;
				}
				case MotionEventCompat.ACTION_POINTER_DOWN: {
					final int index = MotionEventCompat.getActionIndex(ev);
					final float x = MotionEventCompat.getX(ev, index);
					mLastMotionX = x;
					mActivePointerId = MotionEventCompat.getPointerId(ev, index);
					break;
				}
				case MotionEvent.ACTION_MOVE: {

					break;
				}
				case MotionEvent.ACTION_UP:
					if (mActivePointerId != INVALID_POINTER_ID) {
						// Scroll to follow the motion event
						final int activePointerIndex = MotionEventCompat
								.findPointerIndex(ev, mActivePointerId);
						final float x = MotionEventCompat.getX(ev, activePointerIndex);
						final float deltaX = mLastMotionX - x;
						final float oldScrollX = getScrollX();
						final int width = getWidth();
						final int widthWithMargin = width + getPageMargin();
						final int lastItemIndex = getAdapter().getCount() - 1;
						final int currentItemIndex = getCurrentItem();
						final float leftBound = Math.max(0, (currentItemIndex - 1)
								* widthWithMargin);
						final float rightBound = Math.min(currentItemIndex + 1,
								lastItemIndex) * widthWithMargin;
						final float scrollX = oldScrollX + deltaX;
						if (mScrollPositionOffset == 0) {
							if (scrollX < leftBound) {
								if (leftBound == 0) {
									final float over = deltaX + mTouchSlop;
									// mOverscrollEffect.setPull(over / width);
									// System.out.println("---左边第一页-->>");
									if (myDirectListener != null) {
										myDirectListener.getsliderLister(0);
									}
								}
							} else if (scrollX > rightBound) {
								if (rightBound == lastItemIndex * widthWithMargin) {
									final float over = scrollX - rightBound
											- mTouchSlop;
									// System.out.println("---右边最后一页-->>");
									if (myDirectListener != null) {
										myDirectListener.getsliderLister(1);
									}
								}
							}
						} else {
							mLastMotionX = x;
						}
					} else {
					}
					break;
				case MotionEvent.ACTION_CANCEL: {
					mActivePointerId = INVALID_POINTER_ID;
					break;
				}
				case MotionEvent.ACTION_POINTER_UP: {
					final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
					final int pointerId = MotionEventCompat.getPointerId(ev,
							pointerIndex);
					if (pointerId == mActivePointerId) {
						// This was our active pointer going up. Choose a new
						// active pointer and adjust accordingly.
						final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
						mLastMotionX = ev.getX(newPointerIndex);
						mActivePointerId = MotionEventCompat.getPointerId(ev,
								newPointerIndex);
					}
					break;
				}
			}
			return super.onTouchEvent(ev);
		}else{
			return false;
		}
	}
	@Override
	public void setCurrentItem(int item) {
		super.setCurrentItem(item, isCanScroll);
	}

	@Override
	public void setCurrentItem(int item, boolean smoothScroll) {
		super.setCurrentItem(item, smoothScroll);
	}
	public interface MyDirectListener {
		public void getsliderLister(int indexLeftOrRight);
	}

}
