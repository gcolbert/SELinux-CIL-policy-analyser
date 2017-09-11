# SELinux-CIL-policy-analyser
SELinux CIL policy analyser is a simple web API to parse, query and visualize a SELinux CIL policy.

What kind of application is that?
---------------------------------

This application requires NetKernel, a Java micro-kernel implementing the [Resource-Oriented Computing architecture](https://en.wikipedia.org/wiki/Resource-oriented_computing). You can download NetKernel Standard Edition from [1060 Research](http://download.netkernel.org/nkse/ "NetKernel Standard Edition download page").

Installing NetKernel
---------------------

1. To run the application, you first have to install Java 7 or later. Then run NetKernel with:

`java -jar 1060-NetKernel-SE-6.1.1.jar`

or any later version.

2. Open a browser and go to:

[http://localhost:1060](http://localhost:1060/ "NetKernel administration page")

3. Click the button to install NetKernel to disk.

It is easier to install NetKernel in a directory that is a sibling of the `NetKernel-SELinux` folder. If you choose to install it somewhere else, then edit the file `NetKernel-SELinux/etc/modules.d/selinux-modules.xml` with the correct relative paths from NK's installation directory.

4. To fix slow execution or memory problems, please edit the file in :

`bin/jvmsettings.cnf`

and raise the value of the `-Xmx` parameter, for example :

`-server -XX:+UseParallelOldGC -Xmx2048m -Xms256m -Dfile.encoding=UTF-8`

This will use 256 MiB when NetKernel starts and could use up to 2 GiB if needed.

5. Open the file `etc/kernel.properties` in NetKernel's installation directory, and add a line that tells NetKernel where to find the list of modules for the application:

`netkernel.init.modulesdir=../NetKernel-SELinux/etc/modules.d/`

SELinux policy installation
---------------------------
Last step is to install a SELinux policy to analyse.

1. Create a folder of the name of the policy in:

`NetKernel-SELinux/modules/urn.fr.selinux.services/resources/data/`

2. As root, copy the SELinux modules from :

`/etc/selinux/[policyname]/active/modules/100/*`

to the folder you created.

3. Fix permissions of the files so that NetKernel can read them, using the `chown -R` command.

For example with the `targeted` policy and a `user` Unix name, the commands would be:

    `$ mkdir -p NetKernel-SELinux/modules/urn.fr.selinux.services/resources/data/targeted`
    
    `$ cd NetKernel-SELinux/modules/urn.fr.selinux.services/resources/data/targeted`
    
    `$ sudo su`
    
    `[sudo] password for user:`
    
    `# cp -r /etc/selinux/targeted/active/modules/100/* .`
    
    `# chown -R user:user .`

Running
-------

Start NetKernel with the `bin/netkernel.sh` script, and open a browser at the following URLs:

[http://localhost:8080/services/policies/[policyname]](http://localhost:8080/services/policies/[policyname])

and

[http://localhost:8080/visualization/policies/[policyname]](http://localhost:8080/visualization/policies/[policyname])

where [policyname] is the name of the folder containing the SELinux policy (e.g. `targeted` or `mls`).

License
-------

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

Thanks
------

Code of the CIL parser is based on the [Rosetta Code project's "LISP S-expressions" for Java](http://rosettacode.org/wiki/S-Expressions#Java) by Joel F. Klein.

Visualization is based on some [bl.ocks.org](http://bl.ocks.org) examples.
