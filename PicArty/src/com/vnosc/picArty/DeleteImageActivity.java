package com.vnosc.picArty;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import com.vnosc.picArty.adapter.ImageAdapter;
import com.vnosc.picArty.bo.CellGrid;
import com.vnosc.picArty.common.Common;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.AdapterView.OnItemClickListener;

public class DeleteImageActivity extends Activity {
	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.delete_grid_layout);

		context = this;
		display = getWindowManager().getDefaultDisplay();
		display.getWidth();

		File sdDir = new File("/sdcard/PicArty");
		sdDirFiles = sdDir.listFiles();

		btnCancelButton = (Button) findViewById(R.id.btn_cancel);
		btnDeleteButton = (Button) findViewById(R.id.btn_delete_action);
		btnDeleteButton.setEnabled(false);
		gridView = (GridView) findViewById(R.id.grid_view);
		btnGotoCat = (ImageButton) findViewById(R.id.btn_goto_category);
		final Object data = (Object) getLastNonConfigurationInstance();
		if (data == null) {
			readImage();
		} else {
			mySDCardImages = (Vector<CellGrid>) data;
			setDeleteState();
		}

		setImagesAdapter();

		// catch event listenner
		btnCancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((Activity) context).finish();
			}
		});

		btnDeleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setMessage(
						"Permanently Delete Selected Image"
								+ (count() > 1 ? "s" : "") + "?")
						.setCancelable(true)
						.setPositiveButton("Delete",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										for (int i = 0; i < mySDCardImages
												.size(); i++) {
											if (mySDCardImages.get(i)
													.isSelect()) {
												sdDirFiles[i].delete();

											}
										}

										readImage();
										setImagesAdapter();
										setDeleteState();
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});

				AlertDialog alert = builder.create();
				alert.show();
			}
		});

		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				ImageAdapter adapter = (ImageAdapter) arg0.getAdapter();
				// imageView.setScaleType(ImageView.ScaleType.FIT_XY);
				if (!mySDCardImages.get(position).isSelect()) {
					mySDCardImages.get(position).setSelect(true);
				} else {
					mySDCardImages.get(position).setSelect(false);
				}

				setDeleteState();
				adapter.notifyDataSetChanged();
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

	/*
	 * save state when change orientation
	 */
	@Override
	public Object onRetainNonConfigurationInstance() {

		return mySDCardImages;
	}

	public void readImage() {
		mySDCardImages = new Vector<CellGrid>();
		File sdDir = new File("/sdcard/PicArty");
		sdDirFiles = sdDir.listFiles();
		Arrays.sort(sdDirFiles, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return Long.valueOf(f2.lastModified()).compareTo(
						f1.lastModified());
			}
		});
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

	public void setDeleteState() {
		btnDeleteButton.setText(countSelected());
		if (count() == 0) {
			btnDeleteButton.setEnabled(false);
		} else {
			btnDeleteButton.setEnabled(true);
		}
	}

	// setImageAdapter
	public void setImagesAdapter() {

		ViewGroup.LayoutParams gridLayout = gridView.getLayoutParams();
		if (display.getHeight() > display.getWidth()) {
			gridLayout.width = (int) (Common.WIDTH_SCREEN * this.getResources()
					.getDisplayMetrics().density);
			mainAdapter = new ImageAdapter(this, this.getResources()
					.getDisplayMetrics().density, mySDCardImages);
			gridView.setAdapter(mainAdapter);
		} else {
			gridLayout.width = (int) (Common.HEIGHT_SCREEN * this
					.getResources().getDisplayMetrics().density);
			mainAdapter = new ImageAdapter(this, this.getResources()
					.getDisplayMetrics().density, mySDCardImages);
			gridView.setAdapter(mainAdapter);
		}
	}

	public String countSelected() {

		if (count() == 0)
			return "Delete";
		return "Delete(" + count() + ")";
	}

	public int count() {
		int count = 0;
		for (int i = 0; i < mySDCardImages.size(); i++) {
			if (mySDCardImages.get(i).isSelect())
				count++;
		}
		return count;
	}

	private GridView gridView;
	private Button btnCancelButton;
	private Button btnDeleteButton;
	private Display display;
	private Context context;
	private File[] sdDirFiles;
	private ImageButton btnGotoCat;
	private Vector<CellGrid> mySDCardImages;
	private ImageAdapter mainAdapter;
}
