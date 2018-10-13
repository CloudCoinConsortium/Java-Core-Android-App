package com.raida.tech.cloudcoin;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.raida.tech.cloudcoin.raida.RAIDA;
import com.raida.tech.cloudcoin.utilities.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public enum DepositState { DepositInit, DepositIng, DepositDone }

    private LinearLayout linearLayoutDeposit;
    private LinearLayout linearLayoutBank;
    private LinearLayout linearLayoutWithdraw;

    private boolean depositFinished = true;
    public static ExecutorService executor = Executors.newFixedThreadPool(25);
    public static RAIDA[] raidaArray = new RAIDA[25];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void init() {
        for (int i = 0; i < 25; i++) {
            raidaArray[i] = new RAIDA(i);
        }

        linearLayoutDeposit = findViewById(R.id.ldeposit);
        linearLayoutDeposit.setOnClickListener(this);

        linearLayoutBank = findViewById(R.id.lbank);
        linearLayoutBank.setOnClickListener(this);

        linearLayoutWithdraw = findViewById(R.id.lwithdraw);
        linearLayoutWithdraw.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.ldeposit:
                if (!depositFinished) return;

                // check internet connection status
                ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();

                if (netInfo == null || !netInfo.isConnectedOrConnecting())
                {
                    //dialog.Update(Resource.Layout.depositdialog2);
                    //dialog.Show();
                    return;
                }

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

    private void showDepositScreen() {

    }

    private void showBankScreen() {

    }

    private void showWithdrawScreen() {

    }

    /** Sets the Raida array to echo the production servers. */
    public static void initializeRealRaida() {
        for (int i = 0; i < 25; i++) {
            raidaArray[i].switchToRealHost();
        }
    }

    /** Sets the Raida array to echo the test servers. */
    public static void initializeFakeRaida() {
        for (int i = 0; i < 25; i++) {
            raidaArray[i].switchToFakeHost();
        }
    }

    /** Prepares 25 ping tests to RAIDA severs*/
    public static void initializeRaidaEcho() {
        List<Callable<Void>> taskList = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            final int index = i;

            Callable<Void> callable = () -> {
                raidaArray[index].echo();
                System.out.print("." + index);
                return null;
            };
            taskList.add(callable);
        }

        try {
            List<Future<Void>> futureList = executor.invokeAll(taskList);

            for (Future<Void> voidFuture : futureList) {
                try {
                    voidFuture.get(100, TimeUnit.MILLISECONDS);
                } catch (ExecutionException e) {
                    System.out.println("Error executing task " + e.getMessage());
                } catch (TimeoutException e) {
                    System.out.println("Timed out executing task" + e.getMessage());
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /** Sends an echo to each RAIDA server and logs the results. */
    public static void testRaidaEcho() {
        System.out.println("\nEchoing RAIDA.\n");

        initializeRaidaEcho();

        StringBuilder masterFile = new StringBuilder();
        Logger.emptyFolder("Echo");

        for (int i = 0; i < 25; i++) {
            masterFile.append(raidaArray[i].lastJsonRaFromServer);
            masterFile.append("<br>");
            Logger.logFile("Echo", i + "." + raidaArray[i].status + "." + raidaArray[i].msServer + "." +
                    raidaArray[i].ms + ".log", raidaArray[i].lastJsonRaFromServer.getBytes());
            System.out.println("RAIDA" + i + ": " + raidaArray[i].status + ", ms:" + raidaArray[i].ms);
        }

        Logger.logFile("Echo", "echo_log.html", masterFile.toString().getBytes());
    }
}

