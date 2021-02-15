'''
Created on Jan 5, 2021

@author: jayasankarr
'''

import logging
import os

log = logging.getLogger("python_agent")

def add_standard_sub_directories_to_path():
    if os.path.isdir("/sbin"):
        os.environ["PATH"] += os.pathsep + "/sbin"
    if os.path.isdir("/usr/sbin"):
        os.environ["PATH"] += os.pathsep + "/usr/sbin"
    if os.path.isdir("/usr/local/sbin"):
        os.environ["PATH"] += os.pathsep + "/usr/local/sbin"