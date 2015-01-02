package com.llamacorp.unitcalc.view;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.llamacorp.unitcalc.Calculator;
import com.llamacorp.unitcalc.R;
import com.llamacorp.unitcalc.Unit;
import com.llamacorp.unitcalc.UnitHistCurrency;
import com.llamacorp.unitcalc.UnitCurrency.OnConvertKeyUpdateFinishedListener;
import com.llamacorp.unitcalc.UnitType;

public class ConvKeysFragment extends Fragment implements OnConvertKeyUpdateFinishedListener {

	//this is for communication with the parent activity
	OnConvertKeySelectedListener mCallback;

	// Container Activity must implement this interface
	public interface OnConvertKeySelectedListener {
		public void updateScreen(boolean updateResult);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (OnConvertKeySelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnConvertKeySelectedListener");
		}
	}

	//used for the extra
	private static final String EXTRA_UNIT_TYPE_POS = "com.llamacorp.unitcalc.unit_type_pos";
	//holds UnitType for this fragment aka series of convert buttons
	private UnitType mUnitType;
	private ArrayList<Button> mConvButton;

	private int[] convertButtonIds = {	
			R.id.convert_button1,
			R.id.convert_button2,
			R.id.convert_button3,
			R.id.convert_button4,
			R.id.convert_button5,
			R.id.convert_button6,
			R.id.convert_button7,
			R.id.convert_button8,
			R.id.convert_button9,
			R.id.convert_button10};



	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		int pos = getArguments().getInt(EXTRA_UNIT_TYPE_POS);

		mUnitType = Calculator.getCalculator(getActivity()).getUnitType(pos);
		mUnitType.setDynamicUnitCallback(this);
	}

	@Override
	public void onResume(){
		super.onResume();
		colorSelectedButton();
	}

	public static ConvKeysFragment newInstance(int unitTypePos){
		Bundle args = new Bundle();
		args.putInt(EXTRA_UNIT_TYPE_POS, unitTypePos);

		ConvKeysFragment fragment = new ConvKeysFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.fragment_convert_keys, parent, false);

		mConvButton = new ArrayList<Button>();
		final int numButtons = convertButtonIds.length;

		//		if(mUnitType.isDynamicUnit()){
		//			mUnitType.refreshDynamicUnits(getActivity().getApplicationContext());
		//		}		

		for(int i=0; i<numButtons; i++) {
			Button button = (Button)v.findViewById(convertButtonIds[i]);

			if(mUnitType.size()>10)
				((SecondaryTextButton)button).setSecondaryText((String) getText(R.string.conv_button_hint));

			//add to our list of conv buttons
			mConvButton.add(button);			

			//if button is empty, don't create OnClickListener for it
			if(mUnitType.getUnitDisplayName(i).equals(""))
				continue;

			refreshButtonText(i);

			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {

					int viewId = view.getId();
					for (int i=0; i<numButtons; i++){
						if(convertButtonIds[i] == viewId){
							//select key
							clickUnitButton(i);
							//don't continue looking through the button array
							break;
						}
					}

				}
			});

			final int buttonPos = i;
			button.setOnLongClickListener(new View.OnLongClickListener() {
				@Override 
				public boolean onLongClick(View view) {
					//if there are less units to display than slots, move on
					if(mUnitType.size() <= numButtons)
						return true;

					AlertDialog.Builder builder = new AlertDialog.
							Builder(getActivity());
					builder.setTitle(getText(R.string.word_Change) 
							+ " " + mUnitType.getLowercaseLongName(buttonPos) 
							+ " " + getText(R.string.word_to) + ":");
					builder.setItems(mUnitType.getUndisplayedUnitNames(numButtons), 
							new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int item) {
							mUnitType.swapUnits(buttonPos, item+numButtons);
							refreshButtonText(buttonPos);
						}
					});
					//null seems to do the same as canceling the dialog
					builder.setNegativeButton(android.R.string.cancel,null);
					AlertDialog alert = builder.create();
					alert.show();
					return false;
				}
			});

		}
		return v;
	}


	/** Used by parent activity to select a unit within this fragment 
	 * @param unit the Unit selected */
	public void selectUnit(Unit unit) {
		int unitPos = mUnitType.findUnitPosition(unit);
		//unitPos will be -1 if it wasn't found
		if(unitPos != -1 && unitPos < mConvButton.size())
			clickUnitButton(unitPos);
	}

	public void updateDynamicUnitButtons(String text){
		if(!mUnitType.containsDynamicUnits())
			return;
		//add or remove dynamic unit updating indicator
		for(int i=0;i<mConvButton.size();i++){
			//first check if each particular unit is dynamic
			if(mUnitType.isUnitDynamic(i)){
				if(mUnitType.isUnitUpdating(i))
					mConvButton.get(i).setText(text);
				else
					refreshButtonText(i);
			}
		}
	}


	private void refreshButtonText(int buttonPos){
		refreshButtonText("", buttonPos);
	}

	private void refreshButtonText(String textPrefix, int buttonPos){
		String displayText = mUnitType.getUnitDisplayName(buttonPos);
		if(displayText.equals(""))
			return;
		displayText = textPrefix + displayText;
		Button button = mConvButton.get(buttonPos);

		/*
			//want to superscript text after a "^" character
			String [] splitArray = displayText.split("\\^");
			//only upper-case text if it exists
			if(splitArray.length>1){
				//cut out the "^"
				SpannableString spanText = new SpannableString(splitArray[0] + splitArray[1]);   
				//superscript the portion after the "^"
				spanText.setSpan(new SuperscriptSpan(), splitArray[0].length(), spanText.length(), 0);  
				button.setText(spanText, BufferType.SPANNABLE);   
			}
			//otherwise just set it normally
			else
		 */
		button.setText(displayText);
	}
	
	/** Used to pass selected unit to the UnitType model class
	 * @param buttonPos the position in the list of buttons to select */
	private void clickUnitButton(final int buttonPos){
		//pop open selection dialog for historical units
		if(mUnitType.isUnitHistorical(buttonPos)){
			UnitHistCurrency uhc = (UnitHistCurrency) mUnitType.getUnit(buttonPos);
			AlertDialog.Builder builder = new AlertDialog.
					Builder(getActivity());
			builder.setTitle(getText(R.string.word_Change) 
					+ " " + getText(R.string.word_historical) 
					+ " " + mUnitType.getLowercaseLongName(buttonPos) 
					+ " " + getText(R.string.word_to) + ":");
			builder.setItems(uhc.getPossibleYears(), 
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int item) {
					UnitHistCurrency uhc = (UnitHistCurrency) mUnitType.getUnit(buttonPos);
					uhc.setNewYear(item);
					refreshButtonText(buttonPos);
					tryConvert(buttonPos);
				}
			});
			//null seems to do the same as canceling the dialog
			builder.setNegativeButton(android.R.string.cancel,null);
			AlertDialog alert = builder.create();
			alert.show();
		}
		//for historical units, only perform after dialog is gone
		else
			tryConvert(buttonPos);
	}
	
	
	private void tryConvert(int buttonPos){
		//Clear color and arrows from previously selected convert buttons
		clearButtonSelection();
		
		//Set select unit, also this will potentially call convert if we already have a selected unit
		boolean requestConvert = mUnitType.selectUnit(buttonPos);

		Calculator calc = Calculator.getCalculator(getActivity());
		//unit previously selected, we pressed another, do a convert
		if(requestConvert){
			calc.convertFromTo(mUnitType.getPrevUnit(), mUnitType.getCurrUnit());
			clearButtonSelection();
		}
		//unit not previously selected, now select one
		else if(mUnitType.isUnitSelected()){
			//if expression was blank, add a highlighted "1"
			if(calc.isExpressionEmpty()){
				//add in a 1 for user's convenience 
				calc.parseKeyPressed("1");
				//highlight it
				calc.setSelection(0, calc.toString().length());
			}
			colorSelectedButton();
		}

		//always update screen to add/remove unit from expression
		mCallback.updateScreen(true);
	}

	private void colorSelectedButton(){
		//is null when app's onResume calls it (convertkey's onCreate called after activity's onResume)
		if(mUnitType==null)
			return;

		if(mUnitType.isUnitSelected()){
			for(int i=0;i<mConvButton.size();i++){
				if(i != mUnitType.getCurrUnitPos()){
					//\u2192 is an ascii arrow
					refreshButtonText("\u2192 ", i);
				}
			}
			//Add color to newly selected convert button
			mConvButton.get(mUnitType.getCurrUnitPos()).setSelected(true);	
		}			
	}



	/** Clears the button unit selection */
	public void clearButtonSelection(){
		//function may be called before convert key array built, in which case, leave
		if(mConvButton == null) return;

		//remove arrows
		for(int i=0;i<mConvButton.size();i++){
			refreshButtonText(i);
		}

		//Clear color from previously selected convert button
		Button prevSelected = mConvButton.get(mUnitType.getCurrUnitPos());
		prevSelected.setSelected(false);	
		//clear the button in the calc
		//	mUnitType.clearUnitSelection();
	}
}