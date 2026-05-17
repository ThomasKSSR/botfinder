import importlib.util
from pathlib import Path
import pandas as pd


def load_module(path, name):
    spec = importlib.util.spec_from_file_location(name, path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def test_text_count_functions():
    repo_root = Path(__file__).resolve().parents[1]
    module_path = repo_root / "ml-service" / "training" / "experiment_account_model.py"
    mod = load_module(module_path, "experiment_account_model")

    assert mod.count_urls("Visit http://example.com and https://x") == 2
    assert mod.count_urls(None) == 0

    assert mod.count_hashtags_in_text("#a #b") == 2
    assert mod.count_hashtags_in_text(123) == 0

    assert mod.count_mentions_in_text("@user hi @another") == 2
    assert mod.count_mentions_in_text("") == 0

    assert abs(mod.uppercase_ratio("ABCdef") - (3/6)) < 1e-6
    assert mod.uppercase_ratio("") == 0.0
    assert mod.uppercase_ratio(123) == 0.0

    assert mod.hashtags_field_count("#one #two") == 2
    assert mod.hashtags_field_count("") == 0

    assert mod.verified_to_int(True) == 1
    assert mod.verified_to_int(False) == 0
    assert mod.verified_to_int("TRUE") == 1
    assert mod.verified_to_int("false") == 0


def test_safe_numeric_and_build_features():
    repo_root = Path(__file__).resolve().parents[1]
    module_path = repo_root / "ml-service" / "training" / "experiment_account_model.py"
    mod = load_module(module_path, "experiment_account_model")

    df = pd.DataFrame({
        "Tweet": ["Hello #a @b http://x", "ALL CAPS", "", None],
        "Retweet Count": [1, 0, "", None],
        "Mention Count": [1, 2, 0, None],
        "Follower Count": [10, 0, -1, None],
        "Verified": [True, False, "TRUE", ""],
        "Hashtags": ["#a", "#b #c", "", None],
    })

    result = mod.build_features(df)

    # basic shape and some columns
    assert "url_count" in result.columns
    assert "uppercase_ratio" in result.columns
    assert result.shape[0] == 4
    assert result["url_count"].iloc[0] == 1
    assert result["hashtags_field_count"].iloc[1] == 2
    assert result["verified"].iloc[0] == 1
