In order to compile the installer script, you'll need to download and install NSIS installer.

This program can be downloaded from here: http://nsis.sourceforge.net/

It shouldn't matter where you install this program.

The install script can be edited with any text editor, and is the file titled uDigInstallScript.nsi

This file and the other files contained in the Installer Tools.zip archive should all lie in the root of the uDIG install 
directory - that is to say, in the same folder that contains the eclipse directory.

To recompile the installer for a new version of uDig, you'll need to do the following:

- Create a fresh install of the new version of uDig, and make sure to include a JRE directory as a subfolder to the eclipse directory.

- Next, open up uDigInstallScript.nsi, and edit the parts that state the version of uDig.  Taking a quick look, I see reference to the
version made at line numbers 43, and 44.  I think that's it.