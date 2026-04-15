from pathlib import Path

import joblib
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import classification_report, accuracy_score, f1_score
from sklearn.model_selection import train_test_split


DATA_DIR = Path(__file__).resolve().parents[1] / "data" / "account_bots"
MODEL_DIR = Path(__file__).resolve().parents[1] / "trained_models"
MODEL_DIR.mkdir(parents=True, exist_ok=True)

MODEL_PATH = MODEL_DIR / "account_model.joblib"


def count_urls(text: str) -> int:
    if not isinstance(text, str):
        return 0
    return text.lower().count("http://") + text.lower().count("https://")


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


def load_dataset() -> pd.DataFrame:
    csv_files = list(DATA_DIR.glob("*.csv"))
    if not csv_files:
        raise FileNotFoundError(f"No CSV files found in {DATA_DIR}")

    df = pd.read_csv(csv_files[0])
    return df


def build_features(df: pd.DataFrame) -> pd.DataFrame:
    result = pd.DataFrame()

    result["retweet_count"] = pd.to_numeric(df["Retweet Count"], errors="coerce").fillna(0)
    result["mention_count"] = pd.to_numeric(df["Mention Count"], errors="coerce").fillna(0)
    result["follower_count"] = pd.to_numeric(df["Follower Count"], errors="coerce").fillna(0)
    result["verified"] = df["Verified"].apply(verified_to_int)

    result["tweet_length"] = df["Tweet"].fillna("").astype(str).apply(len)
    result["url_count"] = df["Tweet"].fillna("").astype(str).apply(count_urls)
    result["hashtag_count_text"] = df["Tweet"].fillna("").astype(str).apply(count_hashtags_in_text)
    result["mention_symbol_count"] = df["Tweet"].fillna("").astype(str).apply(count_mentions_in_text)
    result["uppercase_ratio"] = df["Tweet"].fillna("").astype(str).apply(uppercase_ratio)

    if "Hashtags" in df.columns:
        result["hashtags_field_count"] = df["Hashtags"].fillna("").apply(hashtags_field_count)
    else:
        result["hashtags_field_count"] = 0

    return result


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

    X_train, X_test, y_train, y_test = train_test_split(
        X,
        y,
        test_size=0.2,
        random_state=42,
        stratify=y
    )

    model = RandomForestClassifier(
        n_estimators=200,
        max_depth=12,
        random_state=42,
        class_weight="balanced"
    )

    model.fit(X_train, y_train)

    y_pred = model.predict(X_test)

    acc = accuracy_score(y_test, y_pred)
    f1 = f1_score(y_test, y_pred)

    print(f"\nAccuracy: {acc:.4f}")
    print(f"F1 Score: {f1:.4f}")
    print("\nClassification report:")
    print(classification_report(y_test, y_pred))

    artifact = {
        "model": model,
        "feature_names": list(X.columns)
    }

    joblib.dump(artifact, MODEL_PATH)
    print(f"\nSaved model to: {MODEL_PATH}")


if __name__ == "__main__":
    main()