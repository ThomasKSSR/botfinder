from pathlib import Path
from typing import List

import joblib
import pandas as pd
from fastapi import FastAPI
from pydantic import BaseModel


app = FastAPI(title="ML Service", version="1.0.0")

BASE_DIR = Path(__file__).resolve().parents[1]
MODEL_PATH = BASE_DIR / "trained_models" / "spam_model.joblib"
TROLL_MODEL_PATH = BASE_DIR / "trained_models" / "troll_model.joblib"
ACCOUNT_MODEL_PATH = BASE_DIR / "trained_models" / "account_model.joblib"

account_model_artifact = None

spam_model = None
troll_model = None
spam_model = None


class TextBatchRequest(BaseModel):
    texts: List[str]


class PredictionItem(BaseModel):
    label: str
    score: float


class PredictionResponse(BaseModel):
    predictions: List[PredictionItem]


@app.on_event("startup")
def load_models():
    global spam_model
    global troll_model
    global account_model_artifact

    if MODEL_PATH.exists():
        spam_model = joblib.load(MODEL_PATH)
        print(f"[ML-SERVICE] Loaded spam model from {MODEL_PATH}")
    else:
        print(f"[ML-SERVICE] Spam model not found at {MODEL_PATH}")

    if TROLL_MODEL_PATH.exists():
        troll_model = joblib.load(TROLL_MODEL_PATH)
        print(f"[ML-SERVICE] Loaded troll model from {TROLL_MODEL_PATH}")
    else:
        print(f"[ML-SERVICE] Troll model not found at {TROLL_MODEL_PATH}")

    if ACCOUNT_MODEL_PATH.exists():
        account_model_artifact = joblib.load(ACCOUNT_MODEL_PATH)
        print(f"[ML-SERVICE] Loaded account model from {ACCOUNT_MODEL_PATH}")
    else:
        print(f"[ML-SERVICE] Account model not found at {ACCOUNT_MODEL_PATH}")


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/predict/spam", response_model=PredictionResponse)
def predict_spam(request: TextBatchRequest):
    if spam_model is None:
        raise RuntimeError("Spam model is not loaded")

    probabilities = spam_model.predict_proba(request.texts)
    predictions_raw = spam_model.predict(request.texts)

    predictions: List[PredictionItem] = []

    for predicted_class, prob_row in zip(predictions_raw, probabilities):
        # CLASS=1 is usually spam in this dataset
        spam_score = float(prob_row[1])

        if int(predicted_class) == 1:
            label = "spam"
            score = spam_score
        else:
            label = "ham"
            score = 1.0 - spam_score

        predictions.append(PredictionItem(label=label, score=score))

    return PredictionResponse(predictions=predictions)


@app.post("/predict/troll", response_model=PredictionResponse)
def predict_troll(request: TextBatchRequest):
    if troll_model is None:
        raise RuntimeError("Troll model is not loaded")

    probabilities = troll_model.predict_proba(request.texts)
    predictions_raw = troll_model.predict(request.texts)

    predictions: List[PredictionItem] = []

    for predicted_class, prob_row in zip(predictions_raw, probabilities):
        troll_score = float(prob_row[1])

        if int(predicted_class) == 1:
            label = "troll"
            score = troll_score
        else:
            label = "normal"
            score = 1.0 - troll_score

        predictions.append(PredictionItem(label=label, score=score))

    return PredictionResponse(predictions=predictions)


class AccountFeaturesRequestItem(BaseModel):
    retweet_count: float
    mention_count: float
    follower_count: float
    verified: int
    tweet_length: float
    url_count: float
    hashtag_count_text: float
    mention_symbol_count: float
    uppercase_ratio: float
    hashtags_field_count: float


class AccountBatchRequest(BaseModel):
    accounts: List[AccountFeaturesRequestItem]

@app.post("/predict/account", response_model=PredictionResponse)
def predict_account(request: AccountBatchRequest):
    if account_model_artifact is None:
        raise RuntimeError("Account model is not loaded")

    model = account_model_artifact["model"]
    feature_names = account_model_artifact["feature_names"]

    rows = []
    for item in request.accounts:
        rows.append({
            "retweet_count": item.retweet_count,
            "mention_count": item.mention_count,
            "follower_count": item.follower_count,
            "verified": item.verified,
            "tweet_length": item.tweet_length,
            "url_count": item.url_count,
            "hashtag_count_text": item.hashtag_count_text,
            "mention_symbol_count": item.mention_symbol_count,
            "uppercase_ratio": item.uppercase_ratio,
            "hashtags_field_count": item.hashtags_field_count
        })

    X = pd.DataFrame(rows)
    X = X[feature_names]

    probabilities = model.predict_proba(X)
    predictions_raw = model.predict(X)

    predictions: List[PredictionItem] = []

    for predicted_class, prob_row in zip(predictions_raw, probabilities):
        bot_score = float(prob_row[1])

        if int(predicted_class) == 1:
            label = "bot"
            score = bot_score
        else:
            label = "human"
            score = 1.0 - bot_score

        predictions.append(PredictionItem(label=label, score=score))

    return PredictionResponse(predictions=predictions)