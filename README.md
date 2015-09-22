---
services:
platforms:
author: azure
---

﻿# Android - Mobile Services - StorageDemo
This is a storage sample which makes use of WIndows Azure Mobile Services and Windows Azure Table and Blob Storage.  Mobile Services uses SQL Database for it's normal data storage so we have to do a bit of additional work in order to access Table and Blob Storage.  This sample was built using Eclipse, the Android SDK, and the Andorid Mobile Services SDK.  It was built using a minimum SDK version of 11 and a target version of 17.  The minimum version was used soley for UI purposes.  All of the data access and storage access can be used with any Android version that supports the Mobile Services SDK (8 and up).

Below you will find requirements and deployment instructions.

**Update: 1-11-2015** - You may also need the **android.permission.READ_EXERNAL_STORAGE** in your manifest file if images are stored on a SD card.

## Requirements
* Eclipse - This sample was built with Eclipse Indigo.
* Android SDK - You can download this from the [Android Developer portal](http://developer.android.com/sdk/index.html).
* Windows Azure Account - Needed to create and run the Mobile Service as well as to create a Storage account.  [Sign up for a free trial](https://www.windowsazure.com/en-us/pricing/free-trial/).

## Source Code Folders
* /source/end - This contains code for the application with Mobile Services and requires client side changes noted below.
* /source/scripts - This contains copies of the server side scripts and requires script changes noted below.

## Additional Resources
I've released two blog posts which walks through the code for this sample.  The [first deals with the server side scripts](http://chrisrisner.com/Mobile-Services-and-Windows-Azure-Storage) and talks about how to connect Mobile Services to Storage.  The [second talks about the Android Client](http://chrisrisner.com/Android-and-Mobile-Services-and-Windows-Azure-Storage) and how to connect that to the Mobile Service.


#Setting up your Mobile Service
After creating your Mobile Service in the Windows Azure Portal, you'll need to create tables named "Tables", "TableRows", "BlobContainers", and "BlobBlobs".  After createing these tables, copy the appropriate scripts over.

#Client Application Changes
In order to run the client applicaiton, you'll need to change a few settings in your application.  After importing the project into Eclipse, open StorageService.java file.  In the constructor method, change the <mobileserviceurl> and <applicationkey> to match the values from the Mobile Service you've created.

#Script Changes
Inside of each of the scripts there are variables set for the Storage account name and key.  You'll need to copy these values from your storage account and replace the "accountname" and "accountkey" strings.

## Contact

For additional questions or feedback, please contact the [team](mailto:chrisner@microsoft.com).
