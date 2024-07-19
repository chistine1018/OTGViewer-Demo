package com.asn.otgviewer_demo;

import com.asn.otgviewer_demo.adapters.UsbFilesAdapter;
import com.github.mjdev.libaums.fs.UsbFile;

/**
 * Created by Anson on 24/07/19.
 */
public class ImageViewer {
    private static ImageViewer mInstance = null;

    private UsbFile mCurrentDirectory;
    private UsbFile mCurrentFile;
    private UsbFilesAdapter mAdapter;


    private ImageViewer() {
        mCurrentFile = null;
        mCurrentDirectory = null;
        mAdapter = null;
    }

    public static ImageViewer getInstance() {
        if (mInstance == null) {
            mInstance = new ImageViewer();
        }
        return mInstance;
    }

    public UsbFile getCurrentDirectory() {
        return this.mCurrentDirectory;
    }

    public UsbFile getCurrentFile() {
        return this.mCurrentFile;
    }

    public void setCurrentDirectory(UsbFile dir) {
        mCurrentDirectory = dir;
    }

    public void setCurrentFile(UsbFile file) {
        mCurrentFile = file;
    }

    public void setAdapter(UsbFilesAdapter adapter) {
        mAdapter = adapter;
    }

    public UsbFilesAdapter getAdapter() {
        return mAdapter;
    }

}
