import os, subprocess
from pathlib import Path

if os.name == 'nt':
    EXTENSION = ".exe"
else:
    EXTENSION = ""

if "EXALT_BIN" not in os.environ:
    print("Please set EXALT_BIN to the path of exc and exd.")
    exit(1)
EXALT_BIN = os.environ["EXALT_BIN"]
EXD = EXALT_BIN + "/exd" + EXTENSION
EXC = EXALT_BIN + "/exc" + EXTENSION

    
if "TEST_SCRIPTS_ROOT" not in os.environ:
    print("Please set TEST_SCRIPTS_ROOT to the location of your test scripts.")
    exit(1)
TEST_SCRIPTS_ROOT = os.environ["TEST_SCRIPTS_ROOT"]


num_successes = 0
num_failures = 0
for f in Path(TEST_SCRIPTS_ROOT).glob("**/*.cmb"):
    target = str(f).replace(TEST_SCRIPTS_ROOT, "")
    result = subprocess.call(["python", "test_script.py", target])
    if result != 0:
        num_failures += 1
    else:
        num_successes += 1
        
print()
print("Done testing.")
print("Successes: " + str(num_successes))
print("Failures: " + str(num_failures))
