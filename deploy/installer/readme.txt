In order to compile the installer script, you'll need to download and install NSIS installer.

This program can be downloaded from here: http://nsis.sourceforge.net/

It shouldn't matter where you install this program.

The install script can be edited with any text editor, and is the file titled uDigInstallScript.nsi

This file and the other files contained in the Installer Tools.zip archive should all lie in the root of the uDIG install 
directory - that is to say, in the same folder that contains the eclipse directory.

To recompile the installer for a new version of uDig, you'll need to do the following:

- Create a fresh install of the new version of uDig, and make sure to include a JRE directory as a subfolder to the eclipse directory.

- Next, open up uDigInstallScript.nsi, and edit the parts that state the version of uDig. This includes the following lines: 44,45,50,55,125,163,218,270,303
-- (Replace VersionXXXX with whatever version you are working with for example 1.2-M4)

