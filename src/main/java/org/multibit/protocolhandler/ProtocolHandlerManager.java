/**
 * Copyright 2013 multibit.org
 *
 * Licensed under the MIT license (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://opensource.org/licenses/mit-license.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.multibit.protocolhandler;

import org.multibit.MultiBit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;


/**
 * ProtocolHandlerManager is responsible for setting up the correct operating system
 * settings for the bitcoin: protocol handling.
 * <p/>
 * Currently for Windows only.
 */
public enum ProtocolHandlerManager {
    INSTANCE;

    private static final Logger log = LoggerFactory.getLogger(ProtocolHandlerManager.class);

    public void initialise() {
        // Windows only.
        // Mac is done in the plist file and Linux is done by a script in the installer.
        if (!(System.getProperty("os.name").startsWith("Win"))) {
            return;
        }

        try {
            log.debug("Starting Windows registry changes at " + (new Date()).toString());
            // Set up the Windows registry entries to handle the bitcoin: protocol using this executable.

            // Work out the directory the windows executable is in.
            // With the Oracle based installer the Jar executable file is in a directory "app" and the
            // Windows executable is in the parent directory.
            // Work out the name of the executable.
            // This is of format "MultiBit-"<version>.exe
            String executableFilename = "MultiBit-" + MultiBit.getBitcoinController().getLocaliser().getVersionNumber() + ".exe";

            String installationDirectoryPath = MultiBit.getBitcoinController().getApplicationDataDirectoryLocator().getInstallationDirectory();
            File installationDirectory = new File(installationDirectoryPath);
            File executableDirectory = installationDirectory.getParentFile();
            String fullExecutablePath = executableDirectory.getAbsolutePath() + File.separator + executableFilename;

            log.debug("executableFilename = " + executableFilename);
            log.debug("fullExecutablePath = " + fullExecutablePath);

            log.debug("Before HKCR\\bitcoin = " + WindowsRegistry.readRegistry("HKCR\\bitcoin", ""));
            log.debug("Before HKCR\\bitcoin, URL Protocol = " + WindowsRegistry.readRegistry("HKCR\\bitcoin", "URL Protocol"));
            log.debug("Before HKCR\\bitcoin, DefaultIcon = " + WindowsRegistry.readRegistry("HKCR\\bitcoin\\DefaultIcon", ""));
            log.debug("Before HKCR\\bitcoin\\shell\\open\\command = " + WindowsRegistry.readRegistry("HKCR\\bitcoin\\shell\\open\\command", ""));

            //log.debug("createKey bitcoin ...");
            //WindowsRegistryUtils.createKey(WindowsRegistryUtils.HKEY_CLASSES_ROOT, "bitcoin");
            log.debug("setRegistry bitcoin.1 ...");
            WindowsRegistry.setRegistry(WindowsRegistry.HKEY_CLASSES_ROOT + "\\bitcoin", "", "URL:Bitcoin Protocol");
            log.debug("setRegistry bitcoin.2 ...");
            WindowsRegistry.setRegistry(WindowsRegistry.HKEY_CLASSES_ROOT + "\\bitcoin", "URL Protocol", "");

            //log.debug("createKey bitcoin\\DefaultIcon ...");
            //WindowsRegistryUtils.createKey(WindowsRegistryUtils.HKEY_CLASSES_ROOT, "bitcoin\\DefaultIcon");
            log.debug("writeStringValue bitcoin\\DefaultIcon ...");
            WindowsRegistry.setRegistry(WindowsRegistry.HKEY_CLASSES_ROOT + "\\bitcoin\\DefaultIcon", "", executableFilename + ",1");

//            log.debug("createKey bitcoin\\shell ...");
//            WindowsRegistryUtils.createKey(WindowsRegistryUtils.HKEY_CLASSES_ROOT, "bitcoin\\shell");
//            log.debug("createKey bitcoin\\shell\\open ...");
//            WindowsRegistryUtils.createKey(WindowsRegistryUtils.HKEY_CLASSES_ROOT, "bitcoin\\shell\\open");
//            log.debug("createKey bitcoin\\shell\\open\\command ...");
//            WindowsRegistryUtils.createKey(WindowsRegistryUtils.HKEY_CLASSES_ROOT, "bitcoin\\shell\\open\\command");

            String openCommand = "\"" + fullExecutablePath + "\" \"%1\"";
            log.debug("setRegistry bitcoin\\shell\\open\\command " + openCommand + "...");
            WindowsRegistry.setRegistry(WindowsRegistry.HKEY_CLASSES_ROOT + "\\bitcoin\\shell\\open\\command", "", openCommand);
            log.debug("done");
            // TODO - set name="UseOriginalUrlEncoding" - uses set of dword

            log.debug("After HKCR\\bitcoin = " + WindowsRegistry.readRegistry("HKCR\\bitcoin", ""));
            log.debug("After HKCR\\bitcoin, URL Protocol = " + WindowsRegistry.readRegistry("HKCR\\bitcoin", "URL Protocol"));
            log.debug("After HKCR\\bitcoin, DefaultIcon = " + WindowsRegistry.readRegistry("HKCR\\bitcoin\\DefaultIcon", ""));
            log.debug("After HKCR\\bitcoin\\shell\\open\\command = " + WindowsRegistry.readRegistry("HKCR\\bitcoin\\shell\\open\\command", ""));
        } catch (IOException e) {
            log.error(e.getClass().getCanonicalName() + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error(e.getClass().getCanonicalName() + e.getMessage());
        } catch (Exception e) {
            log.error(e.getClass().getCanonicalName() + e.getMessage());
        } finally {
            log.debug("Ending Windows registry changes at " + (new Date()).toString());
        }
    }
}

//<!--
//        Require the following structure on Windows
//        see http://msdn.microsoft.com/en-us/library/aa767914(v=vs.85).aspx
//
//        bitcoin.reg:
//        Windows Registry Editor Version 5.00
//
//        [HKEY_CLASSES_ROOT\bitcoin]
//@="URL:Bitcoin Protocol"
//        "URL Protocol"=""
//
//        [HKEY_CLASSES_ROOT\bitcoin\DefaultIcon]
//@="multibit.exe,1"
//
//        [HKEY_CLASSES_ROOT\bitcoin\shell]
//
//        [HKEY_CLASSES_ROOT\bitcoin\shell\open]
//
//        [HKEY_CLASSES_ROOT\bitcoin\shell\open\command]
//@="\"C:\\Program Files\\MultiBit-0.2.0beta4\\multibit.exe\" \"%1\""
//
//
//        -->
//<registry>
//<pack name="MultiBit">
//<key root="HKCR" keypath="bitcoin" />
//<value root="HKCR" name="" keypath="bitcoin" string="URL:Bitcoin Protocol"/>
//<value root="HKCR" name="URL Protocol" keypath="bitcoin" string=""/>
//<value root="HKCR" name="UseOriginalUrlEncoding" keypath="bitcoin" dword="1"/>
//
//<key root="HKCR" keypath="bitcoin\DefaultIcon" />
//<value root="HKCR" name="" keypath="bitcoin\DefaultIcon" string="multibit.exe,1"/>
//
//<key root="HKCR" keypath="bitcoin\shell" />
//
//<key root="HKCR" keypath="bitcoin\shell\open" />
//
//<key root="HKCR" keypath="bitcoin\shell\open\command" />
//<value root="HKCR" keypath="bitcoin\shell\open\command" name="" string="&quot;$INSTALL_PATH\multibit.exe&quot; &quot;%1&quot;" />
//
//</pack>
//</registry>