package com.supermap.desktop.ui.controls.DateTimeComponent;

/**
 * Created by lixiaoyao on 2017/9/5.
 */
public enum DateSpinnerFormat {
	HOUR_MINUTE_SECOND("HH:mm:ss"),          // display time from 00:##:## to 24:##:##
	HOUR_MINUTE_SECOND_HALF("hh:mm:ss"),     // display time from 00:##:## to 12:##:##
	HOUR_MINUTE("HH:mm"),                    // display time from 00:## to 24:##
	HOUR_MINUTE_HALF("hh:mm"),               // display time from 00:## to 12:##
	HOUR("HH"),                              // display time from 00 to 24
	HOUR_HALF("hh");                         // display time from 00 to 12

	private String value;

	private DateSpinnerFormat(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}
