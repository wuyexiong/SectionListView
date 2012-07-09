package com.foound.amazinglistview.demo;

import android.app.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import java.util.*;

import com.foound.widget.*;

public class PaginationDemoActivity extends Activity {
	AmazingListView lsComposer;
	PaginationComposerAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pagination_demo);
		
		lsComposer = (AmazingListView) findViewById(R.id.lsComposer);
		lsComposer.setLoadingView(getLayoutInflater().inflate(R.layout.loading_view, null));
		lsComposer.setAdapter(adapter = new PaginationComposerAdapter());
		
		adapter.notifyMayHaveMorePages();
	}
	
	public void bRefresh_click(View v) {
		adapter.reset();
		adapter.resetPage();
		adapter.notifyMayHaveMorePages();
	}
	
	class PaginationComposerAdapter extends AmazingAdapter {
		List<Composer> list = Data.getRows(1).second;
		private AsyncTask<Integer, Void, Pair<Boolean, List<Composer>>> backgroundTask;

		public void reset() {
			if (backgroundTask != null) backgroundTask.cancel(false);
			
			list = Data.getRows(1).second;
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Composer getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		protected void onNextPageRequested(int page) {
			Log.d(TAG, "Got onNextPageRequested page=" + page);
			
			if (backgroundTask != null) {
				backgroundTask.cancel(false);
			}
			
			backgroundTask = new AsyncTask<Integer, Void, Pair<Boolean, List<Composer>>>() {
				@Override
				protected Pair<Boolean, List<Composer>> doInBackground(Integer... params) {
					int page = params[0];
					
					return Data.getRows(page);
				}
				
				@Override
				protected void onPostExecute(Pair<Boolean, List<Composer>> result) {
					if (isCancelled()) return; 
					
					list.addAll(result.second);
					nextPage();
					notifyDataSetChanged();
					
					if (result.first) {
						// still have more pages
						notifyMayHaveMorePages();
					} else {
						notifyNoMorePages();
					}
				};
			}.execute(page);
		}

		@Override
		protected void bindSectionHeader(View view, int position, boolean displaySectionHeader) {
		}

		@Override
		public View getAmazingView(int position, View convertView, ViewGroup parent) {
			View res = convertView;
			if (res == null) res = getLayoutInflater().inflate(R.layout.item_composer, null);
			
			// we don't have headers, so hide it
			res.findViewById(R.id.header).setVisibility(View.GONE);
			
			TextView lName = (TextView) res.findViewById(R.id.lName);
			TextView lYear = (TextView) res.findViewById(R.id.lYear);
			
			Composer composer = getItem(position);
			lName.setText(composer.name);
			lYear.setText(composer.year);
			
			return res;
		}

		@Override
		public void configurePinnedHeader(View header, int position, int alpha) {
		}

		@Override
		public int getPositionForSection(int section) {
			return 0;
		}

		@Override
		public int getSectionForPosition(int position) {
			return 0;
		}

		@Override
		public Object[] getSections() {
			return null;
		}
		
	}
}