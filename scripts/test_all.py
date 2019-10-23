import os, subprocess, threading
from pathlib import Path

if "EXALT_BIN" not in os.environ:
    print("Please set EXALT_BIN to the path of exc and exd.")
    exit(1)
    
if "TEST_SCRIPTS_ROOT" not in os.environ:
    print("Please set TEST_SCRIPTS_ROOT to the location of your test scripts.")
    exit(1)
TEST_SCRIPTS_ROOT = os.environ["TEST_SCRIPTS_ROOT"]


results_lock = threading.Lock()
num_successes = 0
num_failures = 0
targets = []
failing_scripts = []
for f in Path(TEST_SCRIPTS_ROOT).glob("**/*.cmb"):
    target = str(f).replace(TEST_SCRIPTS_ROOT, "")
    targets.append(target)
        
def test_targets(start, end):
    global num_successes, num_failures
    for i in range(start, end):
        result = subprocess.call(["python", "test_script.py", targets[i], "-d"])
        results_lock.acquire(True)
        if result != 0:
            num_failures += 1
            failing_scripts.append(os.path.basename(targets[i]))
        else:
            num_successes += 1
        results_lock.release()
        
NUM_THREADS = 4
num_targets_per_thread = len(targets) // 4
threads = []
for i in range(0, NUM_THREADS):
    start = i * num_targets_per_thread
    if i == NUM_THREADS - 1:
        end = len(targets)
    else:
        end = start + num_targets_per_thread
    thread = threading.Thread(target=test_targets, args=(start, end))
    threads.append(thread)
    thread.start()

for thread in threads:
    thread.join()
        
print("Done testing.")
print("Successes: " + str(num_successes))
print("Failures: " + str(num_failures))
print("Failed Scripts: " + str(failing_scripts))
