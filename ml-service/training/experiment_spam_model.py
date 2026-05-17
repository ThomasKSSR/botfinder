import glob
from pathlib import Path

import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score, f1_score, precision_score, recall_score, classification_report
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline


DATA_DIR = Path(__file__).resolve().parents[1] / "data" / "youtube_spam"


def evaluate_thresholds(model, X_test, y_test):
    probabilities = model.predict_proba(X_test)[:, 1]

    rows = []

    for threshold in [0.30, 0.40, 0.50, 0.60, 0.70, 0.80, 0.90]:
        y_pred = (probabilities >= threshold).astype(int)

        rows.append({
            "threshold": threshold,
            "accuracy": accuracy_score(y_test, y_pred),
            "f1": f1_score(y_test, y_pred),
            "precision": precision_score(y_test, y_pred),
            "recall": recall_score(y_test, y_pred),
        })

    return pd.DataFrame(rows)

def load_dataset() -> pd.DataFrame:
    csv_files = glob.glob(str(DATA_DIR / "*.csv"))
    if not csv_files:
        raise FileNotFoundError(f"No CSV files found in {DATA_DIR}")

    frames = []
    for file_path in csv_files:
        frames.append(pd.read_csv(file_path))

    return pd.concat(frames, ignore_index=True)


def evaluate_config(name, X_train, X_test, y_train, y_test, tfidf_params, clf_params):
    pipeline = Pipeline([
        ("tfidf", TfidfVectorizer(**tfidf_params)),
        ("clf", LogisticRegression(**clf_params))
    ])

    pipeline.fit(X_train, y_train)
    y_pred = pipeline.predict(X_test)

    result = {
        "name": name,
        "accuracy": accuracy_score(y_test, y_pred),
        "f1": f1_score(y_test, y_pred),
        "precision": precision_score(y_test, y_pred),
        "recall": recall_score(y_test, y_pred),
        "tfidf_params": tfidf_params,
        "clf_params": clf_params,
        "pipeline": pipeline,
    }

    return result


def main():
    df = load_dataset()

    print("Columns found:", list(df.columns))
    print("Total rows:", len(df))

    df = df.dropna(subset=["CONTENT", "CLASS"]).copy()

    X = df["CONTENT"].astype(str)
    y = df["CLASS"].astype(int)

    print("\nLabel distribution:")
    print(y.value_counts())

    X_train, X_test, y_train, y_test = train_test_split(
        X,
        y,
        test_size=0.2,
        random_state=42,
        stratify=y
    )

    experiments = [
        {
            "name": "baseline_unigram_bigram",
            "tfidf": {
                "lowercase": True,
                "stop_words": "english",
                "ngram_range": (1, 2),
                "min_df": 2,
            },
            "clf": {
                "max_iter": 1000,
                "random_state": 42,
            },
        },
        {
            "name": "no_stopwords",
            "tfidf": {
                "lowercase": True,
                "stop_words": None,
                "ngram_range": (1, 2),
                "min_df": 2,
            },
            "clf": {
                "max_iter": 1000,
                "random_state": 42,
            },
        },
        {
            "name": "unigram_bigram_trigram",
            "tfidf": {
                "lowercase": True,
                "stop_words": "english",
                "ngram_range": (1, 3),
                "min_df": 2,
            },
            "clf": {
                "max_iter": 1000,
                "random_state": 42,
            },
        },
        {
            "name": "min_df_1",
            "tfidf": {
                "lowercase": True,
                "stop_words": "english",
                "ngram_range": (1, 2),
                "min_df": 1,
            },
            "clf": {
                "max_iter": 1000,
                "random_state": 42,
            },
        },
        {
            "name": "class_weight_balanced",
            "tfidf": {
                "lowercase": True,
                "stop_words": "english",
                "ngram_range": (1, 2),
                "min_df": 2,
            },
            "clf": {
                "max_iter": 1000,
                "random_state": 42,
                "class_weight": "balanced",
            },
        },
        {
            "name": "stronger_regularization_C_0_5",
            "tfidf": {
                "lowercase": True,
                "stop_words": "english",
                "ngram_range": (1, 2),
                "min_df": 2,
            },
            "clf": {
                "max_iter": 1000,
                "random_state": 42,
                "C": 0.5,
            },
        },
        {
            "name": "weaker_regularization_C_2",
            "tfidf": {
                "lowercase": True,
                "stop_words": "english",
                "ngram_range": (1, 2),
                "min_df": 2,
            },
            "clf": {
                "max_iter": 1000,
                "random_state": 42,
                "C": 2.0,
            },
        },
    ]

    results = []

    best_result = None

    for config in experiments:
        print("\n" + "=" * 80)
        print(f"Running experiment: {config['name']}")

        result = evaluate_config(
            config["name"],
            X_train,
            X_test,
            y_train,
            y_test,
            config["tfidf"],
            config["clf"],
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


    y_pred = best_result["pipeline"].predict(X_test)
    print("\nClassification report for best model:")
    print(classification_report(y_test, y_pred))

    print("\nThreshold comparison for best model:")
    threshold_df = evaluate_thresholds(best_result["pipeline"], X_test, y_test)
    print(threshold_df.to_string(index=False))


if __name__ == "__main__":
    main()