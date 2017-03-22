package tech.behaviouring.pm.hardware.fingerprintreader;

import Cogent.*;
import CogentBioSDK.CgtBioSdkApi;

/*
 * Created by Mohan on 23/1/2016
 */

public class InitFpReader implements Runnable {

	private IDeviceConnected deviceListener;

	public InitFpReader(IDeviceConnected deviceListener) {
		this.deviceListener = deviceListener;
	}

	@Override
	public void run() {
		boolean success = initReader();
		if (success) {
			success = initReaderForFpExtract();
			if (!success)
				return;
		}
		registerDeviceListener();
	}

	private boolean initReader() {
		CgtFpCaptureAccess.cgtFingerPrintDeInit();

		System.out.println("Initializing CSD200");

		int initRet = CgtFpCaptureAccess.cgtFingerPrintInit(CgtFpCaptureAccess.CG4_SCANNER_CSD200);
		if (initRet < 0) {
			System.out.println("Initialization Failed. Error Code: " + initRet);
			System.out.println("Last ErrorCode: " + CgtFpCaptureAccess.cgtFingerPrintGetLastErrorCode());
			return false;

		} else {
			System.out.println("Calibration is in progress.");
			int calibrationRet = CgtFpCaptureAccess.cgtFingerPrintCalibrate();
			if (calibrationRet < 0) {
				System.out.println("Calibration Failed. Error Code: " + calibrationRet);
				return false;

			} else {
				System.out.println("Calibration Success.");
				System.out.println("Initialization Success.");
				return true;
			}
		}
	}

	private boolean initReaderForFpExtract() {
		// The following inits are required for fingerprint extractions

		int initRet = CgtBioSdkApi.initExtract();
		if (CgtBioSdkApi.SUCCESS != initRet) {
			System.out.println("initExtract() is failed. Error Code: " + initRet);
			return false;
		}
		initRet = CgtBioSdkApi.initMatchTemplates();
		if (CgtBioSdkApi.SUCCESS != initRet) {
			System.out.println("initMatchTemplates() is failed. Error Code: " + initRet);
			return false;
		}
		return true;
	}

	private boolean registerDeviceListener() {

		int nRetDevCallback = CgtFpCaptureAccess.cgtRegisterCallbackDeviceConnected(deviceListener);
		System.out.println("Device Listener returns " + nRetDevCallback);
		if (nRetDevCallback < 0) {
			System.out.println("Device Connection Failed. Error Code: " + nRetDevCallback);
			return false;
		} else {
			return true;
		}
	}
}
