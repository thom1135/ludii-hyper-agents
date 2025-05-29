import pandas as pd

import ClusterProcessor
import DataProcessor
from DistanceProcessor import get_distances, maximise_min_distance_game

# load Data
concepts = DataProcessor.load_concepts_data()
ludemes = DataProcessor.load_ludemes_data()
agents = DataProcessor.load_agents_data()
agents_columns = agents.columns

# Scale Concepts
game_names = concepts['GameName']
concepts = DataProcessor.scale_data(DataProcessor.bi_symmetric_log_transform(concepts.drop(columns=['GameName'])))
concepts['GameName'] = game_names

# Merge Concepts/Ludemes
features = pd.merge(concepts, ludemes, on='GameName')
games = features['GameName']

# Filter games with known performance
features_agents = pd.merge(features, agents, on='GameName')
filtered_games = features_agents['GameName']
filtered_games = list(set(games) -  set(filtered_games))

# Extract features for filtered games
filtered_features = features[features['GameName'].isin(filtered_games)]
filtered_features = filtered_features.reset_index(drop=True, inplace=False)

filtered_games = filtered_features["GameName"]
filtered_features = filtered_features.drop(columns=["GameName"])

# Scale features
filtered_features_normalised = DataProcessor.scale_data(filtered_features)

# Calculate cosine distance based on features
distance_matrix = get_distances(filtered_features, 'cosine')

# Select games
top_distance, top_index, starting_game, selected_games = maximise_min_distance_game(distance_matrix, filtered_games, 50)

# Agent regret
agent_regret = pd.read_csv('analysis/AgentRegret.csv')

# Calculate regret for portfolio selections
regret_scores = []
for game in selected_games:
        regret = agent_regret.loc[agent_regret["Game Name"] == game, "Regret"].values[0]
        regret_scores.append(regret)

indices = [index for index, value in enumerate(games) if value in selected_games]
regret_indices = list(zip(regret_scores, indices))

games = features['GameName']
features = features.drop(columns=['GameName'])
features_normalised = DataProcessor.scale_data(features)

X3_tsne = ClusterProcessor.apply_2d_tsne(features_normalised)

optimal_k = ClusterProcessor.calculate_optimal_k(*ClusterProcessor.get_k_wcss(features_normalised, 10))
clusters = ClusterProcessor.get_clusters(features_normalised, optimal_k)
available_indices = [index for index, value in enumerate(games) if value in list(filtered_games)]
selected_indices = [index for index, value in enumerate(games) if value in list(selected_games)]

clusters[available_indices] += 4

clusters[selected_indices] = 8

for score, index in regret_indices:
        clusters[index] = score

ClusterProcessor.plot_2d_heatmap_tsne(X3_tsne, indices, regret_scores)