from pathlib import Path

import joblib
import pandas as pd

from sklearn.ensemble import RandomForestClassifier, GradientBoostingClassifier, ExtraTreesClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score, classification_report, f1_score, precision_score, recall_score
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler


DATA_DIR = Path(__file__).resolve().parents[1] / "data" / "account_bots"
MODEL_DIR = Path(__file__).resolve().parents[1] / "trained_models"
MODEL_DIR.mkdir(parents=True, exist_ok=True)

MODEL_PATH = MODEL_DIR / "account_model_experiment_best.joblib"


def count_urls(text: str) -> int:
    if not isinstance(text, str):
        return 0
    lower = text.lower()
    return lower.count("http://") + lower.count("https://")


def count_hashtags_in_text(text: str) -> int:
    if not isinstance(text, str):
        return 0
    return text.count("#")


def count_mentions_in_text(text: str) -> int:
    if not isinstance(text, str):
        return 0
    return text.count("@")



def uppercase_ratio(text: str) -> float:
    if not isinstance(text, str) or not text.strip():
        return 0.0

    letters = [c for c in text if c.isalpha()]
    if not letters:
        return 0.0

    upper = [c for c in letters if c.isupper()]
    return len(upper) / len(letters)



def hashtags_field_count(value) -> int:
    if not isinstance(value, str) or not value.strip():
        return 0
    return len([part for part in value.split() if part.strip()])



def verified_to_int(value) -> int:
    if isinstance(value, bool):
        return int(value)

    if isinstance(value, str):
        return 1 if value.strip().upper() == "TRUE" else 0

    return 0



def safe_numeric(series) -> pd.Series:
    return pd.to_numeric(series, errors="coerce").fillna(0)



def load_dataset() -> pd.DataFrame:
    csv_files = list(DATA_DIR.glob("*.csv"))

    if not csv_files:
        raise FileNotFoundError(f"No CSV files found in {DATA_DIR}")

    frames = [pd.read_csv(file_path) for file_path in csv_files]
    return pd.concat(frames, ignore_index=True)



def build_features(df: pd.DataFrame) -> pd.DataFrame:
    result = pd.DataFrame()

    tweet = df["Tweet"].fillna("").astype(str)

    result["retweet_count"] = safe_numeric(df["Retweet Count"])
    result["mention_count"] = safe_numeric(df["Mention Count"])
    result["follower_count"] = safe_numeric(df["Follower Count"])
    result["verified"] = df["Verified"].apply(verified_to_int)

    result["tweet_length"] = tweet.apply(len)
    result["url_count"] = tweet.apply(count_urls)
    result["hashtag_count_text"] = tweet.apply(count_hashtags_in_text)
    result["mention_symbol_count"] = tweet.apply(count_mentions_in_text)
    result["uppercase_ratio"] = tweet.apply(uppercase_ratio)

    if "Hashtags" in df.columns:
        result["hashtags_field_count"] = df["Hashtags"].fillna("").apply(hashtags_field_count)
    else:
        result["hashtags_field_count"] = 0

    # Extra ratio/log features. These often help more than raw counts.
    result["log_follower_count"] = result["follower_count"].apply(lambda x: 0 if x <= 0 else pd.NA)
    result["log_follower_count"] = result["follower_count"].clip(lower=0).add(1).apply(lambda x: __import__("math").log1p(x))

    result["retweet_per_follower"] = result["retweet_count"] / (result["follower_count"] + 1)
    result["mentions_per_char"] = result["mention_symbol_count"] / (result["tweet_length"] + 1)
    result["hashtags_per_char"] = result["hashtag_count_text"] / (result["tweet_length"] + 1)

    return result



def evaluate_model(name, model, X_train, X_test, y_train, y_test, scale=False):
    if scale:
        estimator = Pipeline([
            ("scaler", StandardScaler()),
            ("clf", model),
        ])
    else:
        estimator = model

    estimator.fit(X_train, y_train)
    y_pred = estimator.predict(X_test)

    return {
        "name": name,
        "accuracy": accuracy_score(y_test, y_pred),
        "f1": f1_score(y_test, y_pred),
        "precision": precision_score(y_test, y_pred),
        "recall": recall_score(y_test, y_pred),
        "model": estimator,
    }



def main() -> None:
    df = load_dataset()

    print("Columns found:", list(df.columns))
    print("Total rows:", len(df))

    if "Bot Label" not in df.columns:
        raise ValueError("Expected 'Bot Label' column in dataset")

    X = build_features(df)
    y = pd.to_numeric(df["Bot Label"], errors="coerce").fillna(0).astype(int)

    print("\nFeature columns:")
    print(list(X.columns))

    print("\nLabel distribution:")
    print(y.value_counts())

    print("\nFeature summary:")
    print(X.describe().T[["mean", "std", "min", "max"]])

    X_train, X_test, y_train, y_test = train_test_split(
        X,
        y,
        test_size=0.2,
        random_state=42,
        stratify=y,
    )

    experiments = [
        {
            "name": "random_forest_baseline",
            "model": RandomForestClassifier(
                n_estimators=200,
                max_depth=12,
                random_state=42,
                class_weight="balanced",
            ),
            "scale": False,
        },
        {
            "name": "random_forest_deeper",
            "model": RandomForestClassifier(
                n_estimators=400,
                max_depth=None,
                min_samples_leaf=2,
                random_state=42,
                class_weight="balanced",
                n_jobs=-1,
            ),
            "scale": False,
        },
        {
            "name": "extra_trees",
            "model": ExtraTreesClassifier(
                n_estimators=400,
                max_depth=None,
                min_samples_leaf=2,
                random_state=42,
                class_weight="balanced",
                n_jobs=-1,
            ),
            "scale": False,
        },
        {
            "name": "gradient_boosting",
            "model": GradientBoostingClassifier(
                n_estimators=200,
                learning_rate=0.05,
                max_depth=3,
                random_state=42,
            ),
            "scale": False,
        },
        {
            "name": "logistic_regression_scaled",
            "model": LogisticRegression(
                max_iter=1000,
                random_state=42,
                class_weight="balanced",
            ),
            "scale": True,
        },
    ]

    results = []
    best_result = None

    for config in experiments:
        print("\n" + "=" * 80)
        print(f"Running experiment: {config['name']}")

        result = evaluate_model(
            config["name"],
            config["model"],
            X_train,
            X_test,
            y_train,
            y_test,
            scale=config["scale"],
        )

        results.append(result)

        print(f"Accuracy:  {result['accuracy']:.4f}")
        print(f"F1:        {result['f1']:.4f}")
        print(f"Precision: {result['precision']:.4f}")
        print(f"Recall:    {result['recall']:.4f}")

        if best_result is None or result["f1"] > best_result["f1"]:
            best_result = result

    summary = pd.DataFrame([
        {
            "name": r["name"],
            "accuracy": r["accuracy"],
            "f1": r["f1"],
            "precision": r["precision"],
            "recall": r["recall"],
        }
        for r in results
    ]).sort_values(by="f1", ascending=False)

    print("\n" + "=" * 80)
    print("SUMMARY")
    print(summary.to_string(index=False))

    print("\n" + "=" * 80)
    print("BEST MODEL")
    print(best_result["name"])
    print(f"Best F1: {best_result['f1']:.4f}")

    y_pred = best_result["model"].predict(X_test)

    print("\nClassification report for best model:")
    print(classification_report(y_test, y_pred))

    artifact = {
        "model": best_result["model"],
        "feature_names": list(X.columns),
    }

    joblib.dump(artifact, MODEL_PATH)
    print(f"\nSaved best experiment model to: {MODEL_PATH}")


if __name__ == "__main__":
    main()