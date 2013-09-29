package com.jinshisong.track;

public interface ActivityCallback {
	public void success(String message);
	public void error(String message);
	public void setCheckBoxViewEnabled(boolean enbablec);
	public void setCheckBoxChecked(boolean checked);
}
