package com.asn.otgviewer_demo.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.asn.otgviewer_demo.R;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, MenuProvider {

    private static final String enable_transitions = "enable_transitions";
    private static final boolean enable_transitions_def = true;
    private static final String enable_shake = "enable_shake";
    private static final boolean enable_shake_def = true;
    private static final String low_ram = "low_ram";
    private static final boolean low_ram_def = false;
    private static final String showcase_speed = "showcase_speed";
    private static final String showcase_speed_def = "5000"; // medium

    private SettingsCallback mMainActivity;
    private String TAG = getClass().getSimpleName();

    private MenuHost menuHost;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {

            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }

            @Override
            public void onPrepareMenu(@NonNull Menu menu) {
                menu.findItem(R.id.action_settings).setVisible(false);
                MenuProvider.super.onPrepareMenu(menu);
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);

    }


    @Override
    public void onPrepareMenu(@NonNull Menu menu) {
        MenuProvider.super.onPrepareMenu(menu);
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {

    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.settings, rootKey);
    }

    public static boolean areTransitionsEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(enable_transitions, enable_transitions_def);
    }

    public static boolean isShakeEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(enable_shake, enable_shake_def);
    }

    public static boolean isLowRamEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(low_ram, low_ram_def);
    }

    public static int getShowcaseSpeed(Context context) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(showcase_speed, showcase_speed_def));
    }

    public interface SettingsCallback {
        public void setABTitle(String title, boolean showMenu);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String s) {
        // Change something dinamically
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mMainActivity = (SettingsCallback) context;
            updateUI();
        } catch (ClassCastException castException) {
            /** The activity does not implement the listener. */
        }


    }

    private void updateUI() {
        mMainActivity.setABTitle(getString(R.string.settings_title), true);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().invalidateOptionsMenu();

    }
}
