package edu.mit.mitmobile2.emergency.activity;

import android.os.Bundle;

import edu.mit.mitmobile2.MITModuleActivity;
import edu.mit.mitmobile2.R;

public class EmergencyActivity extends MITModuleActivity {
	private static final int CONTENT_LAYOUT_ID = R.layout.fragment_emergency;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.setContentLayoutId(CONTENT_LAYOUT_ID);
		super.onCreate(savedInstanceState);
	}

}