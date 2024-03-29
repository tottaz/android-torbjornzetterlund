package com.app.torbjornzetterlund.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        /**
         * Fires when recycler view receives a single tap event on any item
         *
         * @param view     tapped view
         * @param position item position in the list
         */
        public void onItemClick(View view, int position);

        /**
         * Fires when recycler view receives a long tap event on item
         *
         * @param view     long tapped view
         * @param position item position in the list
         */
        public void onItemLongClick(View view, int position);
    }

    GestureDetector mGestureDetector;
    ExtendedGestureListener mGestureListener;

    public RecyclerItemClickListener(Context context, OnItemClickListener listener) {
        mListener = listener;
        mGestureListener = new ExtendedGestureListener();
        mGestureDetector = new GestureDetector(context, mGestureListener);
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && mListener != null) {
            mGestureListener.setHelpers(childView, view.getChildAdapterPosition(childView));
            mGestureDetector.onTouchEvent(e);
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    /**
     * Extended Gesture listener react for both item clicks and item long clicks
     */
    private class ExtendedGestureListener extends GestureDetector.SimpleOnGestureListener {
        private View view;
        private int position;

        public void setHelpers(View view, int position) {
            this.view = view;
            this.position = position;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            mListener.onItemClick(view, position);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            mListener.onItemLongClick(view, position);
        }
    }
}