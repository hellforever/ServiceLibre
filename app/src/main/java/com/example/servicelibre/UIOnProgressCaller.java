package com.example.servicelibre;
import android.app.ProgressDialog;

import com.arcsolu.sopda.biz.*;

public class UIOnProgressCaller implements OnProgressCaller {
	private ProgressDialog _dlg;
	public void SetProgressDlg(ProgressDialog dlg) {
		_dlg = dlg;
	}
	
	@Override
	public void OnProgress(double pos, double total) {
		// TODO Auto-generated method stub
		if (_dlg != null) {
			_dlg.setMax((int)total);
			_dlg.setProgress((int)pos);
		}
	}

	@Override
	public void OnFinished() {
		// TODO Auto-generated method stub
		if (_dlg != null) {
			_dlg.dismiss();
		}
	}

}
