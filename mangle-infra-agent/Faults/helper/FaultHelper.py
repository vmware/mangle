'''
Created on Jan 5, 2021

@author: jayasankarr
'''

import logging
import os
import subprocess

log = logging.getLogger("python_agent")

def add_standard_sub_directories_to_path():
    if os.path.isdir("/sbin"):
        os.environ["PATH"] += os.pathsep + "/sbin"
    if os.path.isdir("/usr/sbin"):
        os.environ["PATH"] += os.pathsep + "/usr/sbin"
    if os.path.isdir("/usr/local/sbin"):
        os.environ["PATH"] += os.pathsep + "/usr/local/sbin"
    log.info("Path variable:{}".format(os.environ["PATH"]))


def is_sudo_available():
    res = subprocess.call('sudo -nv >/dev/null 2>&1', shell = True)
    sudo_command = ''
    if res == 0:
        sudo_command = 'sudo '
        log.info("sudo available")
    return sudo_command