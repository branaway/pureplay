# Scala
import sys
import inspect
import os
import subprocess
import shutil

from play.utils import *

MODULE = 'scala'

COMMANDS = ['scala:console']

def execute(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")

    if command == "scala:console":
        # add precompiled classes to classpath
        cp_args = app.cp_args() + ":" + os.path.normpath(os.path.join(app.path,'tmp', 'classes'))
        # replace last element with the console app
        java_cmd = app.java_cmd(args, cp_args)
        java_cmd[len(java_cmd)-1] = "play.console.Console"
        subprocess.call(java_cmd, env=os.environ)
        print

def after(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")

    # ~~~~~~~~~~~~~~~~~~~~~~ new
    if command == 'new':
        os.remove(os.path.join(app.path, 'app/controllers/Application.java'))
        module_dir = inspect.getfile(inspect.currentframe()).replace("commands.py", "")
        if not os.path.exists(os.path.join(app.path, 'app/controllers/Application.scala')):
            shutil.copyfile(os.path.join(module_dir, 'resources', 'Application.scala'),
                            os.path.join(app.path, 'app', 'controllers', 'Application.scala'))

    # ~~~~~~~~~~~~~~~~~~~~~~ Eclipsify
    if command == 'ec' or command == 'eclipsify':
        dotProject = os.path.join(app.path, '.project')
        replaceAll(dotProject, r'org\.eclipse\.jdt\.core\.javabuilder', "ch.epfl.lamp.sdt.core.scalabuilder")
        replaceAll(dotProject, r'<natures>', "<natures>\n\t\t<nature>ch.epfl.lamp.sdt.core.scalanature</nature>")
