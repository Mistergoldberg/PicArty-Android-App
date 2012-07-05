package com.vnosc.picArty;

import java.io.File;
import java.util.Vector;

import com.vnosc.picArty.adapter.ImageAdapter;
import com.vnosc.picArty.bo.CellGrid;
import com.vnosc.picArty.common.Common;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class GridImagesActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grid_layout);

		display = getWindowManager().getDefaultDisplay();
		display.getWidth();

		gridView = (GridView) findViewById(R.id.grid_view);
		btnDelete = (ImageButton) findViewById(R.id.btn_delete);
		btnShowCamera = (ImageButton) findViewById(R.id.btn_camera);
		btnGotoCat = (ImageButton) findViewById(R.id.btn_goto_category);

		btnDelete.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),
						DeleteImageActivity.class);
				startActivity(intent);
			}
		});

		btnShowCamera.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),
						PicArtyActivity.class);
				startActivity(intent);
			}
		});

		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				Intent intent = new Intent(getApplicationContext(),
						ShowGalleryActivity.class);
				intent.putExtra("position", position);
				startActivity(intent);
			}
		});

		btnGotoCat.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),
						CategoryActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		readImage();
		setImagesAdapter();
	}

	public void readImage() {
		mySDCardImages = new Vector<CellGrid>();
		File sdDir = new File("/sdcard/PicArty");
		File[] sdDirFiles = sdDir.listFiles();
		for (File singleFile : sdDirFiles) {
			CellGrid cell = new CellGrid();
			cell.setSelect(false);
			// ImageView myImageView = new ImageView(mContext);
			Runtime.getRuntime().gc();
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize = 4;
			Bitmap bitmap = BitmapFactory.decodeFile(
					singleFile.getAbsolutePath(), opts);

			// myImageView.setBackgroundDrawable(new BitmapDrawable(bitmap));
			// myImageView.setId(picIndex);
			cell.setImage(bitmap);
			// drawablesId.add(myImageView.getId());
			mySDCardImages.add(cell);
		}
	}

	/*
	 * public method
	 */
	public void setImagesAdapter() {

		ViewGroup.LayoutParams gridLayout = gridView.getLayoutParams();
		if (display.getHeight() > display.getWidth()) {
			gridLayout.width = (int) (Common.WIDTH_SCREEN * this
					.getResources().getDisplayMetrics().density);

			gridView.setAdapter(new ImageAdapter(this, this.getResources()
					.getDisplayMetrics().density, mySDCardImages));
		} else {
			gridLayout.width = (int) (Common.HEIGHT_SCREEN * this
					.getResources().getDisplayMetrics().density);
			gridView.setAdapter(new ImageAdapter(this, this.getResources()
					.getDisplayMetrics().density, mySDCardImages));
		}
	}

	/*
	 * private variables
	 */
	private ImageButton btnDelete;
	private ImageButton btnShowCamera;
	private GridView gridView;
	private Display display;
	private ImageButton btnGotoCat;
	private Vector<CellGrid> mySDCardImages;
}
