package com.arcsolu.sopda.biz;

public interface OnProgressCaller {
	void OnProgress(double pos, double total);
	void OnFinished();
}
