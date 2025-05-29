from math import log2
import pandas as pd
from sklearn.decomposition import PCA
from sklearn.discriminant_analysis import StandardScaler

def load_agents_data():
    return pd.read_csv('data/input/gameAgents.csv')

def load_heuristics_data():
    return pd.read_csv('data/input/gameHeuristics.csv')

def load_concepts_data():
    return pd.read_csv('data/input/gameConceptsCompilation.csv')

def load_ludemes_data():
    return pd.read_csv('data/input/gameLudemes.csv')

def bi_symmetric_log_transform(data):
    return data.map(lambda x : log2(x + 1) if x >= 0 else -log2(1-x))

def scale_data(data):
    scaler = StandardScaler()
    return pd.DataFrame(scaler.fit_transform(data), columns=data.columns)

def apply_pca(data):
    X = data.drop(columns=['GameName'])
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)
    pca = PCA(n_components=len(data.columns) - 1, random_state=69)
    pca.fit_transform(X_scaled)
    explained_variance = pca.explained_variance_ratio_
    df_components = pd.DataFrame(
        pca.components_, columns=data.drop(columns=['GameName']).columns)
    
    return df_components, explained_variance