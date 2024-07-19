package com.asn.otgviewer_demo;


import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.FragmentManager;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import android.os.Bundle;


import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;

import com.asn.otgviewer_demo.fragments.ExplorerFragment;

import com.asn.otgviewer_demo.fragments.HomeFragment;
import com.asn.otgviewer_demo.fragments.SettingsFragment;
import com.asn.otgviewer_demo.ui.VisibilityManager;
import com.asn.otgviewer_demo.util.Utils;
import com.github.mjdev.libaums.UsbMassStorageDevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.Manifest;

public class MainActivity extends AppCompatActivity implements HomeFragment.HomeCallback, ExplorerFragment.ExplorerCallback, SettingsFragment.SettingsCallback {

    private String TAG = "OTGViewer";

    // 如果要看LOG可設置為True
    private boolean DEBUG = false;

    // 顯示讀取到的ALL USB MASS Device
    private List<UsbDevice> mDetectedDevices;

    // 自定義權限 from com.github.mjdev:libaums:0.5.5
    // PendingIntent 等待權限請求完畢回來的Activity
    // UsbManager 設備管理器 getAllDevice
    // UsbMassStorageDevice U盤
    private static final String ACTION_USB_PERMISSION = "com.androidinspain.otgviewer.USB_PERMISSION";
    private PendingIntent mPermissionIntent;
    private UsbManager mUsbManager;
    private UsbMassStorageDevice mUsbMSDevice;

    // Custom Toolbar
    private Toolbar mToolbar;
    private CoordinatorLayout mCoordinatorLayout;

    // UI Fragment
    private HomeFragment mHomeFragment;
    private SettingsFragment mSettingsFragment;
    private ExplorerFragment mExplorerFragment;
    private VisibilityManager mVisibilityManager;

    private final int HOME_FRAGMENT = 0;
    private final int SETTINGS_FRAGMENT = 1;
    private final int EXPLORER_FRAGMENT = 2;

    private boolean mShowIcon = false;

    // 自定義onBackPressed事件，若是FragmentStack>0 就走自定義;不是就走原生onBackPressed
    private OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();
    private OnBackPressedCallback onBackPressedCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        // 設定自定義Toolbar
        setSupportActionBar(mToolbar);

        if (DEBUG)
            Log.d(TAG, "onCreate");

        mHomeFragment = new HomeFragment();
        mSettingsFragment = new SettingsFragment();
        mExplorerFragment = new ExplorerFragment();

        // 先設定好PendingIntent等待觸發，Flag 基於Android 13以後安全性考量，設置為可改變且可改變舊的
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mDetectedDevices = new ArrayList<UsbDevice>();
        mVisibilityManager = new VisibilityManager();

        // 新版Menu創建方式 透過MenuProvider控管
        MenuHost menuHost = this;
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                getMenuInflater().inflate(R.menu.menu_main, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_settings) {
                    displayView(SETTINGS_FRAGMENT);
                    return true;
                } else if (menuItem.getItemId() == R.id.action_showcase) {
                    return false;
                } else if (menuItem.getItemId() == android.R.id.home) {
                    onBackPressedDispatcher.onBackPressed();
                    return true;
                }

                return false;
            }
        }, this, Lifecycle.State.RESUMED);

        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Catch back action and pops from backstack
                // (if you called previously to addToBackStack() in your transaction)

                boolean result = false;
                if (mExplorerFragment != null && mExplorerFragment.isVisible()) {
                    if (DEBUG)
                        Log.d(TAG, "we are on ExplorerFragment. Result: " + result);

                    result = mExplorerFragment.popUsbFile();
                }

                if (result) {
                    return;
                } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                    if (DEBUG)
                        Log.d(TAG, "Pop fragment");
                }
            }
        };

        displayView(HOME_FRAGMENT);
    }

    public CoordinatorLayout getCoordinatorLayout() {
        return mCoordinatorLayout;
    }

    private void displayView(int position) {
        Fragment fragment = null;

        switch (position) {
            case HOME_FRAGMENT:
                fragment = mHomeFragment;
                break;
            case SETTINGS_FRAGMENT:
                fragment = (Fragment) mSettingsFragment;
                break;
            case EXPLORER_FRAGMENT:
                fragment = (Fragment) mExplorerFragment;
                break;
            default:
                break;
        }

        String tag = Integer.toString(position);

        if (getSupportFragmentManager().findFragmentByTag(tag) != null && getSupportFragmentManager().findFragmentByTag(tag).isVisible()) {
            if (DEBUG)
                Log.d(TAG, "No transition needed. Already in that fragment!");

            return;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.animator.enter, R.animator.exit, R.animator.pop_enter, R.animator.pop_exit);

            fragmentTransaction.replace(R.id.container_body, fragment, tag);

            // Home fragment is not added to the stack
            if (position != HOME_FRAGMENT) {
                fragmentTransaction.addToBackStack(null);
            }

            fragmentTransaction.commitAllowingStateLoss();

            getSupportFragmentManager().executePendingTransactions();

            // 監聽Fragment Stack 如果是0，則將onBackPressed Flag拉為False使用原生onBackPressed
            fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    int backStackEntryCount = fragmentManager.getBackStackEntryCount();
                    if (backStackEntryCount == 0) {
                        onBackPressedCallback.setEnabled(false);
                    } else {
                        onBackPressedCallback.setEnabled(true);
                    }
                }
            });
        }


    }

    // 如果OTG偵測到移除，更新UI
    private void removeUSB() {

        if (mVisibilityManager.getIsVisible()) {
            while (getSupportFragmentManager().getBackStackEntryCount() != 0) {
                getSupportFragmentManager().popBackStackImmediate();
            }

            displayView(HOME_FRAGMENT);
        } else {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

    // Receiver 聽OTG插拔, 自定義Action
    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DEBUG)
                Log.d(TAG, "mUsbReceiver triggered. Action " + action);

            checkUSBStatus();

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                removeUSB();
            }

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            //call method to set up device communication
                            if (DEBUG) {
                                Log.d(TAG, "granted permission for device " + device.getDeviceName() + "!");
                            }
                            connectDevice(device);
                        }
                    } else {
                        Log.e(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };

    private void checkUSBStatus() {
        if (DEBUG)
            Log.d(TAG, "checkUSBStatus");

        mDetectedDevices.clear();

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        if (mUsbManager != null) {
            HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();

            if (!deviceList.isEmpty()) {
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                while (deviceIterator.hasNext()) {
                    UsbDevice device = deviceIterator.next();
                    if (Utils.isMassStorageDevice(device)) {
                        mDetectedDevices.add(device);
                    }
                }
            }

            updateUI();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (DEBUG)
            Log.d(TAG, "onResume");

        mVisibilityManager.setIsVisible(true);

        // 動態註冊BroadCastReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_USB_PERMISSION);

        registerReceiver(mUsbReceiver, filter);

        checkUSBStatus();

        onBackPressedDispatcher.addCallback(onBackPressedCallback);
        onBackPressedCallback.setEnabled(false);

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (DEBUG)
            Log.d(TAG, "onPause");

        mVisibilityManager.setIsVisible(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mUsbReceiver);
        Utils.deleteCache(getCacheDir());
    }

    private void updateUI() {
        if (DEBUG)
            Log.d(TAG, "updateUI");

        if (mHomeFragment != null && mHomeFragment.isAdded()) {
            mHomeFragment.updateUI();
            Log.i(TAG, "updateUI: update");
        }
    }

    private void connectDevice(UsbDevice device) {
        if (DEBUG)
            Log.d(TAG, "Connecting to device");

        // 檢查該設備有無權限
        if (mUsbManager.hasPermission(device) && DEBUG)
            Log.d(TAG, "got permission!");

        // 一般手機只有1個OTG接口，所以我們用第一個
        UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(this);
        if (devices.length > 0) {
            mUsbMSDevice = devices[0];
            setupDevice();
        }
    }

    private void setupDevice() {
        displayView(EXPLORER_FRAGMENT);
    }

    @Override
    public void requestPermission(int pos) {
        mUsbManager.requestPermission(mDetectedDevices.get(pos), mPermissionIntent);
    }

    @Override
    public List<UsbDevice> getUsbDevices() {
        return mDetectedDevices;
    }

    @Override
    public void setABTitle(String title, boolean showMenu) {
        // Set ActionBar Title
        getSupportActionBar().setTitle(title);

        // 左上角返回建
        getSupportActionBar().setDisplayHomeAsUpEnabled(showMenu);

        // Set logo true顯示 false不顯示 因為沒有正常Size icon故不顯示
//        getSupportActionBar().setDisplayShowHomeEnabled(showMenu);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
    }


}