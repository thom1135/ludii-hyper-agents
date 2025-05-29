import pandas as pd

import DataProcessor
from DistanceProcessor import get_distances, maximise_min_distance_game, print_formatted_list

concepts = DataProcessor.load_concepts_data()
ludemes = DataProcessor.load_ludemes_data()
agents = DataProcessor.load_agents_data()
agents_columns = agents.columns

game_names = concepts['GameName']
concepts = DataProcessor.scale_data(DataProcessor.bi_symmetric_log_transform(concepts.drop(columns=['GameName'])))
concepts['GameName'] = game_names

features = pd.merge(concepts, ludemes, on='GameName')
games = features['GameName']

features_agents = pd.merge(features, agents, on='GameName')
filtered_games = features_agents['GameName']
filtered_games = list(set(games) -  set(filtered_games))

filtered_features = features[features['GameName'].isin(filtered_games)]
filtered_features = filtered_features.reset_index(drop=True, inplace=False)
filtered_games = filtered_features["GameName"]
filtered_features = filtered_features.drop(columns=["GameName"])

filtered_features_normalised = DataProcessor.scale_data(filtered_features)

distance_matrix = get_distances(filtered_features, 'cosine')

top_distance, top_index, starting_game, selected_games = maximise_min_distance_game(distance_matrix, filtered_games, 50)

# Print selected games with .lud
print_formatted_list(selected_games)