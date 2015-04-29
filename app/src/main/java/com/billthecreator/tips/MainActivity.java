package com.billthecreator.tips;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;


public class MainActivity extends ActionBarActivity {


    private static final String[] servicesArray = {"Bartender", "Delivery", "Waiter", "Cab driver", "Chauffeur", "Hairdresser", "Manicurist", "Spa service", "Masseuse"};


    private InputMethodManager imm;

    //    private DecimalFormat d = new DecimalFormat("'$'#,##0.##");
    private NumberFormat d = NumberFormat.getCurrencyInstance();

    private DecimalFormat p = new DecimalFormat("0%");
    private DecimalFormat p3 = new DecimalFormat("0.###%");

    private static EditText totalAmountEditText, tipPercentEditText;

    private SwitchCompat roundUpCB;
    private int savedRound;

    private TextView tipAmountHeader, totalAmountHeader, tipAmount, totalWithTip;

    private TextView billTextView, tipTextView;

    private TextView billAmountTextView, tipAmountTextView, billSplitTextView;

    private TextView tipPercentRoundedTextView;

    private TextView numPeopleHeader;

    private Toolbar toolbar;

    private LinearLayout billCard;

    private SeekBar numPeoplePaying;

    private int round = 0;

    private SharedPreferences myPrefs;

    private TextView serviceType;

    private ShakeListener mShaker;

    private boolean activityActive = true;


    // menu top right

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);


        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_clearFields:
                resetFields();
                return true;
//            case R.id.action_changeColor:
//                Intent i = new Intent(MainActivity.this, changeColor.class);
//                startActivity(i);

            case R.id.action_settings:
                Intent i = new Intent(MainActivity.this, serviceSettings.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityActive = true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        activityActive = false;
    }


    private void resetFields() {
        totalAmountEditText.setText("0");
        billTextView.setText(d.format(0));
//        tipPercentEditText.setText("0");
//        tipTextView.setText(p.format(0));
        numPeoplePaying.setProgress(0);
        roundUpCB.setChecked(false);
        textOnChange();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {

        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {

            changeLabelStatus(tipPercentEditText, tipTextView, 0);
            changeLabelStatus(totalAmountEditText, billTextView, 0);
        }

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        final String savedTotal;
        final String savedTip;

        final Typeface robotoFontRegular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        final Typeface robotoFontMedium = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        Typeface robotoFontLight = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");

        // Grab all settings
        myPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        savedTotal = myPrefs.getString("savedTotal", "");
        savedTip = myPrefs.getString("savedTip", "");
        savedRound = myPrefs.getInt("savedRound", 0);

        final String pref_bartend = myPrefs.getString("bartender_text", String.valueOf(R.string.pref_service_default_bartender));
        final String pref_delivery = myPrefs.getString("delivery_text", String.valueOf(R.string.pref_service_default_delivery_driver));
        final String pref_waiter = myPrefs.getString("waiter_text", String.valueOf(R.string.pref_service_default_waiter));
        final String pref_cab_driver = myPrefs.getString("chauffeur_text", String.valueOf(R.string.pref_service_default_cab_driver));
        final String pref_chauffeur = myPrefs.getString("chauffeur_text", String.valueOf(R.string.pref_service_default_chauffeur));
        final String pref_hairdresser = myPrefs.getString("hairdresser_text", String.valueOf(R.string.pref_service_default_hairdresser));
        final String pref_manicurist = myPrefs.getString("manicurist_text", String.valueOf(R.string.pref_service_default_manicurist));
        final String pref_spa_service = myPrefs.getString("spa_text", String.valueOf(R.string.pref_service_default_spa_service));
        final String pref_masseuse = myPrefs.getString("masseuse_text", String.valueOf(R.string.pref_service_default_masseuse));

        final int pref_last_service_selected = myPrefs.getInt("service_selected", 2);


        // Show  main window
        setContentView(R.layout.activity_main);


        final Spinner serviceSpinner = (Spinner) findViewById(R.id.servicesSpinner);

        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(this, R.layout.service_spinner, servicesArray) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                ((TextView) v).setTypeface(robotoFontRegular);

                return v;
            }
        };
        mAdapter.setDropDownViewResource(R.layout.service_spinner);
        serviceSpinner.setAdapter(mAdapter);
        serviceSpinner.setSelection(pref_last_service_selected, true);

        // define views
        totalAmountEditText = (EditText) findViewById(R.id.totalTextBox);
        totalAmountEditText.setTypeface(robotoFontRegular);

        tipPercentEditText = (EditText) findViewById(R.id.tipPercentAmount);
        tipPercentEditText.setTypeface(robotoFontRegular);

        roundUpCB = (SwitchCompat) findViewById(R.id.roundSwitch);
        roundUpCB.setTypeface(robotoFontRegular);

        tipAmountHeader = (TextView) findViewById(R.id.tipAmountTextHeader);
        tipAmountHeader.setTypeface(robotoFontRegular);

        totalAmountHeader = (TextView) findViewById(R.id.totalAmountTextHeader);
        totalAmountHeader.setTypeface(robotoFontRegular);

        tipAmount = (TextView) findViewById(R.id.tipAmount);
        tipAmount.setTypeface(robotoFontLight);

        totalWithTip = (TextView) findViewById(R.id.totalAmount);
        totalWithTip.setTypeface(robotoFontLight);

        billTextView = (TextView) findViewById(R.id.billTextView);
        billTextView.setTypeface(robotoFontMedium);

        tipTextView = (TextView) findViewById(R.id.tipTextView);
        tipTextView.setTypeface(robotoFontMedium);

        billAmountTextView = (TextView) findViewById(R.id.billAmountTextView);
        billAmountTextView.setTypeface(robotoFontMedium);

        tipAmountTextView = (TextView) findViewById(R.id.tipAmountTextView);
        tipAmountTextView.setTypeface(robotoFontMedium);

        billSplitTextView = (TextView) findViewById(R.id.billSplitTextView);
        billSplitTextView.setTypeface(robotoFontMedium);

        tipPercentRoundedTextView = (TextView) findViewById(R.id.tipPercentRoundedTextView);
        tipPercentRoundedTextView.setTypeface(robotoFontRegular);

        numPeopleHeader = (TextView) findViewById(R.id.numPeopleHeader);
        numPeopleHeader.setTypeface(robotoFontMedium);

        numPeoplePaying = (SeekBar) findViewById(R.id.seekBar);

        serviceType = (TextView) findViewById(R.id.serviceType);
        serviceType.setTypeface(robotoFontMedium);


        // redefine toolbar
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);


        mShaker = new ShakeListener(this);
        mShaker.setOnShakeListener(new ShakeListener.OnShakeListener() {
            public void onShake() {
                final boolean pref_shake = myPrefs.getBoolean("pref_enable_shake_reset", true);

                if (pref_shake && activityActive) {

                    resetFields();
                    Toast.makeText(getApplicationContext(), "Reset", Toast.LENGTH_SHORT).show();
                }


            }
        });


        // set values from settings to editTexts and TextViews
        if (savedTotal.isEmpty()) {
            totalAmountEditText.setText("");
            billTextView.setText(d.format(Double.parseDouble("0")));
        } else {
            totalAmountEditText.setText(savedTotal);
            billTextView.setText(d.format(Double.parseDouble(savedTotal)));
        }
        if (savedTip.isEmpty()) {
            tipPercentEditText.setText("");
            tipTextView.setText(p.format(Double.parseDouble("0") / 100));
        } else {
            tipPercentEditText.setText(savedTip);
            tipTextView.setText(p.format(Double.parseDouble(savedTip) / 100));
        }


        textOnChange();
        // end setup

        if (savedRound == 1)
            roundUpCB.setChecked(true);

        setTipAndTotal(savedTotal, savedTip, savedRound);

        tipPercentEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    Double tipAmount = 0D;
                    if (!tipPercentEditText.getText().toString().isEmpty()) {
                        tipAmount = Double.parseDouble(tipPercentEditText.getText().toString());
                    }
                    String formatedTip = p.format(tipAmount / 100);
                    tipTextView.setText(formatedTip);
                    changeLabelStatus(tipPercentEditText, tipTextView, 0);

                    imm.hideSoftInputFromWindow(tipPercentEditText.getWindowToken(), 0);
                }
            }
        });

        totalAmountEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    Double billAmount = 0D;
                    if (!totalAmountEditText.getText().toString().isEmpty()) {
                        billAmount = Double.parseDouble(totalAmountEditText.getText().toString());
                    }
                    String formatedBill = d.format(billAmount);
                    billTextView.setText(formatedBill);
                    changeLabelStatus(totalAmountEditText, billTextView, 0);
                    imm.hideSoftInputFromWindow(totalAmountEditText.getWindowToken(), 0);
                }
            }
        });
        // Change to the tip text
        totalAmountEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                textOnChange();
            }
        });

        // Change to the tip text
        tipPercentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                textOnChange();

            }
        });

        numPeoplePaying.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    numPeopleHeader.setText((progress + 1) + " person");
                } else {
                    numPeopleHeader.setText((progress + 1) + " people");
                }
                textOnChange();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        serviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                final String pref_bartend = myPrefs.getString("bartender_text", String.valueOf(R.string.pref_service_default_bartender));
                final String pref_delivery = myPrefs.getString("delivery_text", String.valueOf(R.string.pref_service_default_delivery_driver));
                final String pref_waiter = myPrefs.getString("waiter_text", String.valueOf(R.string.pref_service_default_waiter));
                final String pref_cab_driver = myPrefs.getString("chauffeur_text", String.valueOf(R.string.pref_service_default_cab_driver));
                final String pref_chauffeur = myPrefs.getString("chauffeur_text", String.valueOf(R.string.pref_service_default_chauffeur));
                final String pref_hairdresser = myPrefs.getString("hairdresser_text", String.valueOf(R.string.pref_service_default_hairdresser));
                final String pref_manicurist = myPrefs.getString("manicurist_text", String.valueOf(R.string.pref_service_default_manicurist));
                final String pref_spa_service = myPrefs.getString("spa_text", String.valueOf(R.string.pref_service_default_spa_service));
                final String pref_masseuse = myPrefs.getString("masseuse_text", String.valueOf(R.string.pref_service_default_masseuse));

                String getTip = "0";
                switch (parentView.getSelectedItem().toString().toLowerCase()) {
                    case "bartender":
                        getTip = pref_bartend;
                        break;
                    case "delivery":
                        getTip = pref_delivery;
                        break;
                    case "waiter":
                        getTip = pref_waiter;
                        break;
                    case "cab driver":
                        getTip = pref_cab_driver;
                        break;
                    case "chauffeur":
                        getTip = pref_chauffeur;
                        break;
                    case "hairdresser":
                        getTip = pref_hairdresser;
                        break;
                    case "manicurist":
                        getTip = pref_manicurist;
                        break;
                    case "spa service":
                        getTip = pref_spa_service;
                        break;
                    case "masseuse":
                        getTip = pref_masseuse;
                        break;
                }

                tipPercentEditText.setText(getTip);
                tipTextView.setText(p.format(Double.parseDouble(getTip) / 100));


                myPrefs.edit().putInt("service_selected", position).apply();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        billTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLabelStatus(totalAmountEditText, billTextView, 1);
                totalAmountEditText.requestFocus();
                totalAmountEditText.setSelection(0, totalAmountEditText.getText().length());
                imm.showSoftInput(totalAmountEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        tipTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLabelStatus(tipPercentEditText, tipTextView, 1);
                tipPercentEditText.requestFocus();
                tipPercentEditText.setSelection(0, tipPercentEditText.getText().length());
                imm.showSoftInput(tipPercentEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        billAmountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLabelStatus(totalAmountEditText, billTextView, 0);
            }
        });
        tipAmountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLabelStatus(tipPercentEditText, tipTextView, 0);
            }
        });

        roundUpCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (totalAmountEditText.getText().length() > 0 && tipPercentEditText.getText().length() > 0) {
                        savedRound = 1;
                        setTipAndTotal(totalAmountEditText, tipPercentEditText, savedRound);
                    }
                } else {
                    if (totalAmountEditText.getText().length() > 0 && tipPercentEditText.getText().length() > 0) {
                        savedRound = 0;
                        setTipAndTotal(totalAmountEditText, tipPercentEditText, savedRound);
                    }
                }

                changeLabelStatus(tipPercentEditText, tipTextView, 0);
                changeLabelStatus(totalAmountEditText, billTextView, 0);
                saveSettings();
            }
        });


    }

    private void textOnChange() {
        if (totalAmountEditText.getText().length() > 0 && tipPercentEditText.getText().length() > 0) {
            if (!totalAmountEditText.getText().toString().startsWith(".")) {
                setTipAndTotal(totalAmountEditText, tipPercentEditText, savedRound);
                roundUpCB.setEnabled(true);
//            roundUpCard.setVisibility(View.VISIBLE);
            } else if (totalAmountEditText.getText().toString().startsWith(".")) {
                String getText = totalAmountEditText.getText().toString();
                totalAmountEditText.setText("0" + getText);
                totalAmountEditText.setSelection(totalAmountEditText.length());
            }
        } else {
            roundUpCB.setEnabled(false);
//            roundUpCard.setVisibility(View.GONE);
        }

        saveSettings();
    }

    private void saveSettings() {
        String total = totalAmountEditText.getText().toString();
        String tip = tipPercentEditText.getText().toString();
        int round = savedRound;

        myPrefs.edit().putString("savedTotal", total).apply();
        myPrefs.edit().putString("savedTip", tip).apply();
        myPrefs.edit().putInt("savedRound", round).apply();

    }

    private void changeLabelStatus(EditText e, TextView t, int editShow) {

        switch (editShow) {
            case 0://show text view, hide edit
                t.setVisibility(View.VISIBLE);
                e.setVisibility(View.INVISIBLE);
                break;
            default:// show edit, hide text view
                t.setVisibility(View.INVISIBLE);
                e.setVisibility(View.VISIBLE);
                break;
        }

    }

    private void setTipAndTotal() {
        setTipAndTotal("0", "0");

    }

    private void setTipAndTotal(EditText totalEditText, EditText tipEditText, int round) {

        final String totalE = totalEditText.getText().toString();
        final String tipE = tipEditText.getText().toString();
        final int numPpl = numPeoplePaying.getProgress();

        setTipAndTotal(totalE, tipE, round, numPpl);
    }

    private void setTipAndTotal(String totalEditText, String tipEditText) {
        setTipAndTotal(totalEditText, tipEditText, 0);
    }

    private void setTipAndTotal(String totalEditText, String tipEditText, int round) {
        final int numPpl = numPeoplePaying.getProgress();
        setTipAndTotal(totalEditText, tipEditText, round, numPpl);
    }

    private void setTipAndTotal(String totalEditText, String tipEditText, int round, int numPpl) {

        // correct the seekBar.
        numPpl += 1;

        if (totalEditText.length() == 0 || tipEditText.length() == 0)
            return;

        // get the numbers
        // the amount the user typed for bill total
        double total = Double.parseDouble(totalEditText);
        // the amount the user typed for the tip percent
        double tipPercent = Double.parseDouble(tipEditText) / 100;

        // find the tip amount
        String tip = "" + d.format(total * tipPercent);
        String roundedTipPercent;

        // split the check
        // divide the total from the amount of ppl
        double splitTotal = (total * (1 + tipPercent)) / numPpl;
        // divide the tip form the amount of ppl
        double splitTip = (total * (tipPercent)) / numPpl;


        switch (round) {
            case 1: // rounded

                // round the total up to the next dollar

                double totalCeil = Math.ceil(total * (1 + tipPercent));
                double splitRoundTotal = totalCeil / numPpl;

                double billSplit = (total / numPpl);

                double roundTotal = (Math.ceil(total * (1 + tipPercent) / numPpl));

                double roundTotalMinusTip = (Math.ceil(total)) / numPpl;

                // find the tip from the rounded total
                double roundTip = (splitRoundTotal - billSplit);

                roundedTipPercent = "" + p3.format(roundTip / billSplit);

                totalWithTip.setText(d.format(roundTotal)); // Total rounded
                tipAmount.setText(d.format(roundTip)); // tip amount from rounded total
                tipPercentRoundedTextView.setText(roundedTipPercent);
//                billSplitTextView.setText(d.format(roundTotalMinusTip));

                billSplitTextView.setText(d.format(billSplit));
                break;
            case 0: // not rounded

                totalWithTip.setText(d.format(splitTotal)); // Total with tip
                tipAmount.setText(d.format(splitTip)); // the tip
                tipPercentRoundedTextView.setText(p.format(tipPercent));
                billSplitTextView.setText(d.format((total) / numPpl));
                break;

            default:

                totalWithTip.setText("$0.00"); // Total with tip
                tipAmount.setText("$0.00"); // the tip
                tipPercentRoundedTextView.setText(p.format(0));
                billSplitTextView.setText(d.format(0));
                break;
        }


        String twt = "" + d.format(total * (1 + tipPercent));


        saveSettings();
    }

}