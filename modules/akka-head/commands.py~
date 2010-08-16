import sys
import inspect
import os
import subprocess
import shutil

from play.utils import *

MODULE = 'akka'
COMMANDS = []


def execute(**kargs):

def after(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")

    # ~~~~~~~~~~~~~~~~~~~~~~ new
    if command == 'new':
        module_dir = inspect.getfile(inspect.currentframe()).replace("commands.py", "")
        if not os.path.exists(os.path.join(app.path, 'conf/akka.conf')):
            shutil.copyfile(os.path.join(module_dir, 'resources', 'akka.conf'),os.path.join(app.path, 'conf', 'akka.conf'))

