from scipy.spatial.distance import pdist, squareform
import pandas as pd
import numpy as np
from math import log2
from sklearn.decomposition import PCA
from sklearn.discriminant_analysis import StandardScaler


def bi_symmetric_log_transform(x):
    return log2(x + 1) if x >= 0 else -log2(1-x)

def get_distances(df, metric):
    return squareform(pdist(df, metric=metric))

def remove_index(selected, index):
    for item in selected:
        item[index] = 0

    return selected


def get_pca_df(df, num_components):
    df_transposed = df.set_index('GameName').T.reset_index()
    df_transposed = df_transposed.drop(
        columns=['index'])
    X = df_transposed.to_numpy()
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)
    pca = PCA(n_components=num_components, random_state=69)
    principal_components = pca.fit_transform(X_scaled)
    explained_variance = pca.explained_variance_ratio_
    df_components = pd.DataFrame(
        pca.components_, columns=df_transposed.columns)
    df_components = df_components.T.reset_index()
    
    return df_components, explained_variance


def greedy_distance(orig_matrix, gamenames, num_games, start_index=False):
    selected_rows = []
    games_list = []
    matrix = orig_matrix.copy()
    distance = 0
    if not start_index:
        game1_index, game2_index = np.unravel_index(
            np.argmax(matrix), matrix.shape)
        distance += matrix[game1_index, game2_index]
        matrix[game1_index, game2_index] = 0
        matrix[game2_index, game1_index] = 0
        selected_rows.append(matrix[game1_index, :])
        selected_rows.append(matrix[game2_index, :])
        games_list.append(gamenames[game1_index])
        games_list.append(gamenames[game2_index])
    else:
        selected_rows.append(matrix[start_index, :])
        selected_rows = remove_index(selected_rows, start_index)
        games_list.append(gamenames[start_index])
    result_matrix = np.array(selected_rows)
    while len(games_list) < num_games:
        next_index = np.argmax(np.sum(result_matrix, axis=0))
        distance += np.sum(result_matrix[:, next_index])
        selected_rows.append(matrix[next_index])
        selected_rows = remove_index(selected_rows, next_index)
        if (gamenames[next_index] in games_list):
            selected_rows = remove_index(selected_rows, next_index)
        else:
            games_list.append(gamenames[next_index])
        result_matrix = np.array(selected_rows)

    return games_list, distance

def maximise_greedy_distance(matrix, games, num_games):
    max_dist = 0
    result_games = []
    for i in range(len(games)):
        games_list, distance = greedy_distance(matrix, games, num_games, i)
        if distance > max_dist:
            max_dist = distance
            result_games = games_list
    return result_games, max_dist


def maximise_min_distance(orig_matrix, gamenames, num_games, start_index=False):
    selected_rows = []
    games_list = []
    matrix = orig_matrix.copy()
    if not start_index:
        game1_index, game2_index = np.unravel_index(
            np.argmax(matrix), matrix.shape)
        matrix[game1_index, game2_index] = 0
        matrix[game2_index, game1_index] = 0
        selected_rows.append(matrix[game1_index, :])
        selected_rows.append(matrix[game2_index, :])
        games_list.append(gamenames[game1_index])
        games_list.append(gamenames[game2_index])
    else:
        selected_rows.append(matrix[start_index, :])
        games_list.append(gamenames[start_index])
    result_matrix = np.array(selected_rows)
    min_distance = 0
    while len(games_list) < num_games:
        next_index = np.argmax(np.amin(result_matrix, axis=0))
        min_distance += 0 if np.shape(result_matrix)[0] < 2 else np.amin(result_matrix, axis=0)[next_index]
        selected_rows.append(matrix[next_index])
        selected_rows = remove_index(selected_rows, next_index)
        if (gamenames[next_index] in games_list):
            selected_rows = remove_index(selected_rows, next_index)
        else:
            games_list.append(gamenames[next_index])
        result_matrix = np.array(selected_rows)

    return games_list, min_distance


def count_included_features(df, gamenames):
    filtered_df = df[df['GameName'].isin(gamenames)]

    return len(filtered_df.columns[~filtered_df.eq(0).all()]) - 1


def count_total_possible_features(df):
    return len(df.columns[~df.eq(0).all()]) - 1


def count_all_features(df):
    return len(df.columns) - 1


def print_formatted_list(games):
    print('("', end="")
    for index, item in enumerate(games):
        print(', "' if index != 0 else "", item, end='.lud"', sep="")
    print(')', end="")


def maximise_distance_coverage(matrix, game_names, num_games, concepts_data, ludemes_data):
    c_index, l_index, c_count, cl_count, l_count, lc_count, b_count, b_index = 0, 0, 0, 0, 0, 0, 0, 0
    for i in range(len(game_names)):
        results = greedy_distance(
            matrix, game_names, num_games, i)
        concepts = count_included_features(concepts_data, results)
        ludemes = count_included_features(ludemes_data, results)
        if concepts > c_count:
            c_count = concepts
            c_index = i
        if ludemes > l_count:
            l_count = ludemes
            l_index = i
        if concepts + ludemes > b_count:
            b_index = i
            b_count = concepts + ludemes

    return c_index, l_index, b_index


def maximise_min_distance_game(matrix, game_names, num_games):
    top_distance = 0
    top_index = 0
    starting_game = game_names[0]
    result_games = []
    for i in range(len(game_names)):
        games, distance = maximise_min_distance(
            matrix, game_names, num_games, i)
        if distance > top_distance:
            top_index = i
            starting_game = game_names[i]
            result_games = games
            top_distance = distance
            
    return top_distance, top_index, starting_game, result_games


def maximise_coverage(df, num_games):
    result_games = []
    game_names = list(df['GameName'])
    df_new = df.drop(columns=['GameName'])
    while (len(result_games) < num_games):
        condition = df_new > 0
        row_counts = condition.sum(axis=1)
        max_count_index = row_counts.idxmax()
        result_games.append(game_names[max_count_index])
        df_new = df_new.loc[:, ~condition.iloc[max_count_index]]

    return result_games