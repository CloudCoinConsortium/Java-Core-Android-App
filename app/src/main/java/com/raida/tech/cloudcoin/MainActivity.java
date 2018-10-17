package com.raida.tech.cloudcoin;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.raida.tech.cloudcoin.core.FileSystem;
import com.raida.tech.cloudcoin.raida.RAIDA;
import com.raida.tech.cloudcoin.utils.RealPathUtil;
import com.raida.tech.cloudcoin.utils.SimpleLogger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.raida.tech.cloudcoin.raida.RAIDA.updateLog;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public enum DepositState { DepositInit, DepositIng, DepositDone }
    final static int REQUEST_CODE_IMPORT_DIR = 1;

    private LinearLayout linearLayoutDeposit;
    private LinearLayout linearLayoutBank;
    private LinearLayout linearLayoutWithdraw;

    private DepositState depositState;
    private boolean depositFinished = true;
    private boolean asyncFinished = true;
    private boolean isDepositDialog = false;

    private List<String> files = new ArrayList<>();

    public static int NetworkNumber = 1;
    public static SimpleLogger logger;

    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearLayoutDeposit = findViewById(R.id.ldeposit);
        linearLayoutDeposit.setOnClickListener(this);

        linearLayoutBank = findViewById(R.id.lbank);
        linearLayoutBank.setOnClickListener(this);

        linearLayoutWithdraw = findViewById(R.id.lwithdraw);
        linearLayoutWithdraw.setOnClickListener(this);

        init();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void init() {
        FileSystem.createDirectories();
        RAIDA.getInstance();
        FileSystem.loadFileSystem();

        logger = new SimpleLogger(FileSystem.LogsFolder + "logs" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")).toLowerCase() + ".log", true);

        //Connect to Trusted Trade Socket
        //tts = new TrustedTradeSocket("wss://escrow.cloudcoin.digital/ws/", 10, OnWord, OnStatusChange, OnReceive, OnProgress);
        //tts.Connect().Wait();
        RAIDA.logger = logger;

        updateLog("Loading Network Directory");
        SetupRAIDA();
        FileSystem.loadFileSystem();

        asyncFinished = true;
        depositState = DepositState.DepositInit;

    }

    public static void SetupRAIDA() {
        try
        {
            RAIDA.instantiate();
        }
        catch(Exception e)
        {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
            System.exit(1);
        }
        if (RAIDA.networks.size() == 0)
        {
            updateLog("No Valid Network found.Quitting!!");
            System.exit(1);
        }
        else
        {
            updateLog(RAIDA.networks.size() + " Networks found.");
            RAIDA raida = RAIDA.networks.get(0);
            for (RAIDA r : RAIDA.networks)
                if (NetworkNumber == r.networkNumber) {
                    raida = r;
                    break;
                }

            RAIDA.activeRAIDA = raida;
            if (raida == null) {
                updateLog("Selected Network Number not found. Quitting.");
                System.exit(0);
            }
        }
        //networks[0]
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.ldeposit:
                if (!asyncFinished) return;

                // check internet connection status
                ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();

                if (netInfo == null || !netInfo.isConnectedOrConnecting())
                {
                    //dialog.Update(Resource.Layout.depositdialog2);
                    //dialog.Show();
                    return;
                }

                files.clear();
                showDepositScreen();
                break;

            case R.id.lbank:
                showBankScreen();
                break;

            case R.id.lwithdraw:
                showWithdrawScreen();
                break;

            default:
                break;

        }

        //mSettings = PreferenceManager.GetDefaultSharedPreferences(this);
        String version = "";
        try
        {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        ((TextView)findViewById(R.id.tversion)).setText(version);

        /*asyncFinished = true;
        depositState = DepositState.DepositInit;
        dialog = new CoinDialog(this, Resource.Layout.depositdialog);
        dialog.Create();

        string path;
        if (Android.OS.Environment.ExternalStorageState.Equals(Android.OS.Environment.MediaMounted))
        {
            path = Android.OS.Environment.ExternalStorageDirectory.AbsolutePath + System.IO.Path.DirectorySeparatorChar + "CloudCoin";
        }
        else
        {
            //string path = Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments);
            path = Environment.GetFolderPath(Environment.SpecialFolder.Personal);
        }
        Log.Info("Path", path);
        bank = new Banker(new FileSystem(path));
        bank.fileUtils.CreateDirectories();
        bank.fileUtils.LoadFileSystem();
        CoreLogger.initCoreLogger(bank.fileUtils.LogsFolder);

        initNetworks();*/
    }

    private void initDialog(int layout) {
        if (isDepositDialog) return;
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layout);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        LinearLayout closeButton = (LinearLayout) dialog.findViewById(R.id.closebutton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }

    public void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Coins"), REQUEST_CODE_IMPORT_DIR);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMPORT_DIR) {
            if((resultCode == RESULT_OK) && (data != null)) {
                Uri uri;
                files.clear();
                String file;

                if (data.getData() != null)
                {
                    uri = data.getData();
                    file = RealPathUtil.getRealPath(this, uri);
                    String[] path = file.split(":");
                    if (path.length > 1)
                        files.add(path[1]);
                    else
                        files.add(file);
                    //files.Add(file);
                }
                else
                {
                    if (data.getClipData() != null)
                    {
                        ClipData mClipData = data.getClipData();
                        List<Uri> mArrayUri = new ArrayList<>();
                        for (int i = 0; i < mClipData.getItemCount(); i++)
                        {

                            ClipData.Item item = mClipData.getItemAt(i);
                            uri = item.getUri();
                            file = RealPathUtil.getRealPath(this, uri);
                            String[] path = file.split(":");
                            if (path.length > 1)
                                files.add(path[1]);
                            else
                                files.add(file);
                        }
                        Log.v("LOG_TAG", "Selected Images" + mArrayUri.size());
                    }
                }
            } else {
                //			showError("Internal error");
            }

            dialog.dismiss();
            isDepositDialog = false;
            showDepositScreen();
            return;
        }

        //bank.moveExportedToSent();
        dialog.dismiss();
    }

    private void showDepositScreen() {
        int totalIncomeLength;
        String result;
        TextView ttv;
        TextView tv;
        //FileSystem FS = bank.fileUtils;

        switch (depositState)
        {
            case DepositIng:
                break;

            case DepositDone:
                break;

            case DepositInit:
                    /*if (FS.suspectCoins.Count() > 0)
                    {
                        dialog.Update(Resource.Layout.Depositsuspect);
                        LinearLayout goButton = dialog.FindViewById<LinearLayout>(Resource.Id.gobutton);
                        goButton.Click += async delegate
                        {
                            isDepositSuspect = true;
                            await DepositTask();
                        };
                        dialog.Show();
                        return;
                    }*/

                if (files != null && files.size() > 0)
                {
                    /*foreach (string file in files)
                    {
                        IEnumerable<CloudCoin> coins = FS.LoadCoins(file);
                        if (coins != null)
                        {
                            FS.WriteCoinsToFile(coins, FS.DepositFolder + System.IO.Path.GetFileName(file));
                            FS.LoadFileSystem();
                        }
                        File.Delete(file);
                    }*/
                }
                else
                {
                    //FS.DepositCoins = FS.LoadFolderCoins(FS.DepositFolder);
                }

                initDialog(R.layout.depositdialog);
                LinearLayout fileButton = (LinearLayout) dialog.findViewById(R.id.filebutton);
                fileButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        selectFile();
                    }
                });
                break;
        }




        System.out.println("Processing Network Coins...");
        RAIDA.processNetworkCoins(NetworkNumber);

    }

    private void showBankScreen() {

    }

    private void showWithdrawScreen() {

    }
}

