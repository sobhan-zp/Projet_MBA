/*
 *  ARSimple.java
 *  ARToolKit5
 *
 *  Disclaimer: IMPORTANT:  This Daqri software is supplied to you by Daqri
 *  LLC ("Daqri") in consideration of your agreement to the following
 *  terms, and your use, installation, modification or redistribution of
 *  this Daqri software constitutes acceptance of these terms.  If you do
 *  not agree with these terms, please do not use, install, modify or
 *  redistribute this Daqri software.
 *
 *  In consideration of your agreement to abide by the following terms, and
 *  subject to these terms, Daqri grants you a personal, non-exclusive
 *  license, under Daqri's copyrights in this original Daqri software (the
 *  "Daqri Software"), to use, reproduce, modify and redistribute the Daqri
 *  Software, with or without modifications, in source and/or binary forms;
 *  provided that if you redistribute the Daqri Software in its entirety and
 *  without modifications, you must retain this notice and the following
 *  text and disclaimers in all such redistributions of the Daqri Software.
 *  Neither the name, trademarks, service marks or logos of Daqri LLC may
 *  be used to endorse or promote products derived from the Daqri Software
 *  without specific prior written permission from Daqri.  Except as
 *  expressly stated in this notice, no other rights or licenses, express or
 *  implied, are granted by Daqri herein, including but not limited to any
 *  patent rights that may be infringed by your derivative works or by other
 *  works in which the Daqri Software may be incorporated.
 *
 *  The Daqri Software is provided by Daqri on an "AS IS" basis.  DAQRI
 *  MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 *  THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS
 *  FOR A PARTICULAR PURPOSE, REGARDING THE DAQRI SOFTWARE OR ITS USE AND
 *  OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS.
 *
 *  IN NO EVENT SHALL DAQRI BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL
 *  OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION,
 *  MODIFICATION AND/OR DISTRIBUTION OF THE DAQRI SOFTWARE, HOWEVER CAUSED
 *  AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE),
 *  STRICT LIABILITY OR OTHERWISE, EVEN IF DAQRI HAS BEEN ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 *
 *  Copyright 2015 Daqri, LLC.
 *  Copyright 2011-2015 ARToolworks, Inc.
 *
 *  Author(s): Julian Looser, Philip Lamb
 *
 */

package fr.norips.ar.ARMuseum;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.artoolkit.ar.base.ARActivity;
import org.artoolkit.ar.base.camera.CaptureCameraPreview;
import org.artoolkit.ar.base.rendering.ARRenderer;

import fr.norips.ar.ARMuseum.R;
import fr.norips.ar.ARMuseum.Config.ConfigHolder;
import fr.norips.ar.ARMuseum.Config.JSONParser;

/**
 * A very simple example of extending ARActivity to create a new AR application.
 */

public class ARMuseumActivity extends ARActivity {

    private final static int REQUEST_WRITE = 1;
    private ProgressDialog pDialog;
    public static boolean dismisspDialog = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //Calls ARActivity's ctor, abstract class of ARBaseLib
        setContentView(R.layout.main);
        FrameLayout f = (FrameLayout) findViewById(R.id.mainLayout);
        f.setOnTouchListener(new OnSwipeTouchListener(ARMuseumActivity.this){
            public void onSwipeTop() {
                Toast.makeText(ARMuseumActivity.this, "top", Toast.LENGTH_SHORT).show();
            }
            public void onSwipeRight() {
                ConfigHolder.getInstance().nextPage();
            }
            public void onSwipeLeft() {
                ConfigHolder.getInstance().previousPage();
            }
            public void onSwipeBottom() {
            }
        });
        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE))
                {
                    if (this.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    {
                        // Will drop in here if user denied permissions access camera before.
                        // Or no uses-permission CAMERA element is in the
                        // manifest file. Must explain to the end user why the app wants
                        // permissions to the camera devices.
                        Toast.makeText(this.getApplicationContext(),
                                "App requires access to write external storage to be granted",
                                Toast.LENGTH_SHORT).show();
                    }
                    // Request permission from the user to access the camera.
                    Log.i(TAG, "ARMuseumActivity(): must ask user for write external storage access permission");
                    this.requestPermissions(new String[]
                                    {
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    },
                            REQUEST_WRITE);
                    return;
                }
            }
        }
        catch (Exception ex)
        {
            Log.e(TAG, "CaptureCameraPreview(): exception caught, " + ex.getMessage());
            return;
        }
        pDialog = new ProgressDialog(ARMuseumActivity.this);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult(): called");
        if (requestCode == REQUEST_WRITE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),
                        "Application will not run with folder access denied",
                        Toast.LENGTH_LONG).show();
            }
            else if (1 <= permissions.length) {
                Toast.makeText(getApplicationContext(),
                        String.format("Reading file access permission \"%s\" allowed", permissions[0]),
                        Toast.LENGTH_SHORT).show();
                pDialog = new ProgressDialog(ARMuseumActivity.this);
                JSONParser json = new JSONParser(this.getApplicationContext(), pDialog);
                boolean result = json.createConfig("http://192.168.1.75/format.json", "http://norips.ddns.net/format.json");
            }
            CaptureCameraPreview previewHook = getCameraPreview();
            if (null != previewHook) {
                Log.i(TAG, "onRequestPermissionsResult(): reset ask for cam access perm");
                previewHook.resetGettingCameraAccessPermissionsFromUserState();
            }
        }
        else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    synchronized public void onFrameProcessed() {
        if(dismisspDialog){
            pDialog.dismiss();
        }
    }
    /**
     * Provide our own SimpleRenderer.
     */
    @Override
    protected ARRenderer supplyRenderer() {
        return new SimpleRenderer(this.getBaseContext(),pDialog);
    }

    /**
     * Use the FrameLayout in this Activity's UI.
     */
    @Override
    protected FrameLayout supplyFrameLayout() {
        return (FrameLayout) this.findViewById(R.id.mainLayout);
    }
}