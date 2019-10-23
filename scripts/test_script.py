import os, sys, subprocess

if "EXALT_BIN" not in os.environ:
    print("Please set EXALT_BIN to the path of exc and exd.")
    exit(1)
EXALT_BIN = os.environ["EXALT_BIN"]
EXD = EXALT_BIN + "/exd.jar"
EXC = EXALT_BIN + "/exc.jar"

    
if "TEST_SCRIPTS_ROOT" not in os.environ:
    print("Please set TEST_SCRIPTS_ROOT to the location of your test scripts.")
    exit(1)
TEST_SCRIPTS_ROOT = os.environ["TEST_SCRIPTS_ROOT"]
    
if len(sys.argv) < 2:
    print("Please pass in the path of target script.")
    exit(1)
    
DISABLE_OUTPUT = len(sys.argv) > 2
def output(msg = ""):
    if not DISABLE_OUTPUT:
        print(msg)
    
TARGET_NAME = sys.argv[1]
OUT_NAME = os.path.basename(TARGET_NAME)
SCRIPT_NAME = OUT_NAME + ".exl"

TARGET_SCRIPT_PATH = TEST_SCRIPTS_ROOT + "/" + TARGET_NAME

output("-----------------------------------------")
output("Testing script " + TARGET_NAME)
output("-----------------------------------------")

output("Decompiling target.")
result = subprocess.call(["java", "-jar", EXD, TARGET_SCRIPT_PATH, "-o", SCRIPT_NAME])
if result != 0:
    output("FAILURE: Unable to decompile input. Exiting...")
    exit(1)
    
output("Successfully decompiled target.")
output()

output("Compiling target.")
result = subprocess.call(["java", "-jar", EXC, SCRIPT_NAME, "-o", OUT_NAME])
if result != 0:
    output("FAILURE: Unable to compile input. Exiting...")
    exit(1)
    
output("Successfully compiled target.")
output()

output("Performing binary comparison on input and output...")
with open(TARGET_SCRIPT_PATH, "rb") as f:
    input_bin = f.read()
with open(OUT_NAME, "rb") as f:
    output_bin = f.read()
    
if input_bin != output_bin:
    output("FAILURE: Input/Output do not equal!")
    exit(1)
output("SUCCESS!")
    
