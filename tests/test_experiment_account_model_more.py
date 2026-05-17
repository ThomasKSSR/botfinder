import importlib.util
from pathlib import Path
import pandas as pd
import numpy as np


def load_module(path, name):
    spec = importlib.util.spec_from_file_location(name, path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def test_safe_numeric_and_log_follower(tmp_path):
    repo_root = Path(__file__).resolve().parents[1]
    module_path = repo_root / "ml-service" / "training" / "experiment_account_model.py"
    mod = load_module(module_path, "experiment_account_model")

    s = pd.Series(["1", "x", None, "3.5"])
    out = mod.safe_numeric(s)
    assert out.dtype.kind in ("i","f")
    assert out.iloc[1] == 0

    # test log_follower_count generation via build_features
    df = pd.DataFrame({
        "Tweet": ["a"],
        "Retweet Count": [0],
        "Mention Count": [0],
        "Follower Count": [0],
        "Verified": [False],
    })
    res = mod.build_features(df)
    assert "log_follower_count" in res.columns


def test_load_dataset_raises_when_empty(tmp_path):
    repo_root = Path(__file__).resolve().parents[1]
    module_path = repo_root / "ml-service" / "training" / "experiment_account_model.py"
    mod = load_module(module_path, "experiment_account_model")

    # point DATA_DIR to empty tmp dir
    mod.DATA_DIR = tmp_path
    try:
        mod.load_dataset()
        assert False, "Expected FileNotFoundError"
    except FileNotFoundError:
        pass


def test_evaluate_model_with_dummy():
    from sklearn.dummy import DummyClassifier
    repo_root = Path(__file__).resolve().parents[1]
    module_path = repo_root / "ml-service" / "training" / "experiment_account_model.py"
    mod = load_module(module_path, "experiment_account_model")

    X = pd.DataFrame({"a":[1,2,3,4]})
    y = pd.Series([0,1,0,1])

    model = DummyClassifier(strategy="most_frequent")
    result = mod.evaluate_model("dummy", model, X, X, y, y, scale=False)
    assert set(["name","accuracy","f1","precision","recall","model"]).issubset(result.keys())
