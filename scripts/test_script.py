import os, sys, subprocess

if os.name == 'nt':
    EXTENSION = ".exe"
else:
    EXTENSION = ""

if "EXALT_BIN" not in os.environ:
    print("Please set EXALT_BIN to the path of exc and exd.")
    exit(1)
EXALT_BIN = os.environ["EXALT_BIN"]
EXD = EXALT_BIN + "/exd.jar" + EXTENSION
EXC = EXALT_BIN + "/exc.jar" + EXTENSION

    
if "TEST_SCRIPTS_ROOT" not in os.environ:
    print("Please set TEST_SCRIPTS_ROOT to the location of your test scripts.")
    exit(1)
TEST_SCRIPTS_ROOT = os.environ["TEST_SCRIPTS_ROOT"]
    
if len(sys.argv) < 2:
    print("Please pass in the path of target script.")
    exit(1)
TARGET_NAME = sys.argv[1]
OUT_NAME = os.path.basename(TARGET_NAME)

TARGET_SCRIPT_PATH = TEST_SCRIPTS_ROOT + "/" + TARGET_NAME

print("-----------------------------------------")
print("Testing script " + TARGET_NAME)
print("-----------------------------------------")

print("Decompiling target.")
result = subprocess.call(["java", "-jar", EXD, TARGET_SCRIPT_PATH])
if result != 0:
    print("FAILURE: Unable to decompile input. Exiting...")
    exit(1)
    
print("Successfully decompiled target.")
print()

print("Compiling target.")
result = subprocess.call(["java", "-jar", EXC, "a.exl", "-o", OUT_NAME])
if result != 0:
    print("FAILURE: Unable to compile input. Exiting...")
    exit(1)
    
print("Successfully compiled target.")
print()

print("Performing binary comparison on input and output...")
with open(TARGET_SCRIPT_PATH, "rb") as f:
    input_bin = f.read()
with open(OUT_NAME, "rb") as f:
    output_bin = f.read()
    
if input_bin != output_bin:
    print("FAILURE: Input/Output do not equal!")
    exit(1)
print("SUCCESS!")
    
