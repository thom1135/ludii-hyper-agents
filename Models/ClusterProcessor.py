from matplotlib import pyplot as plt
from matplotlib.colors import ListedColormap
import numpy as np
import pandas as pd

from sklearn.cluster import KMeans
from sklearn.manifold import TSNE
import plotly.express as px

def get_k_wcss(data, test_range):
    k_values = range(1, test_range)
    wcss = []

    for k in k_values:
        kmeans = KMeans(n_clusters=k, init='k-means++', max_iter=1000, n_init='auto', random_state=69)
        kmeans.fit(data)
        wcss.append(kmeans.inertia_)
    
    return k_values, wcss

def plot_k_elbow(k_values, wcss):
    plt.figure()
    plt.plot(k_values, wcss, marker='o')
    plt.title('K Elbow')
    plt.xlabel('Number of clusters (k)')
    plt.ylabel('WCSS')
    plt.xticks(k_values)
    plt.grid(True)
    plt.show()

def calculate_optimal_k(k_values, wcss):
    second_diffs = np.diff(wcss, 2)
    elbow_index = np.argmax(second_diffs) + 1

    return k_values[elbow_index]

def get_clusters(data, k):
    return KMeans(n_clusters=k, random_state=69, n_init='auto',).fit_predict(data)

def apply_2d_tsne(data):
    tsne = TSNE(n_components=2, random_state=69, metric='cosine')
    return tsne.fit_transform(data)

def apply_3d_tsne(data):
    tsne = TSNE(n_components=3, random_state=69, metric='cosine')
    return tsne.fit_transform(data)

def plot_2d_tsne(tsne, clusters):
    plt.figure(figsize=(8, 6))
    color_map_selected = ListedColormap([
        (0, 0, 0, 1),
        (0, 0, 0, 1),
        (0, 0, 0, 1),
        (0, 0, 0, 1),
        (0, 0, 0, 1),
        (0, 0, 0, 1),
        (0, 0, 0, 1),
        (0, 0, 0, 1),
        (1, 1, 0, 1)
    ])
    color_map_distinct = ListedColormap([
        (55/255, 161/255, 251/255, 1),
        (53/255, 179/255, 57/255, 1),
        (251/255, 53/255, 53/255, 1),
        (255/255, 126/255, 38/255, 1),
        (55/255, 161/255, 251/255, 1),
        (53/255, 179/255, 57/255, 1),
        (251/255, 53/255, 53/255, 1),
        (255/255, 126/255, 38/255, 1),
        (1, 1, 0, 1)
    ])

    color_map_available = ListedColormap([
        (55/255, 161/255, 251/255, 0.2),
        (55/255, 161/255, 251/255, 0.2),
        (55/255, 161/255, 251/255, 0.2),
        (55/255, 161/255, 251/255, 0.2),
        (55/255, 161/255, 251/255, 1),
        (55/255, 161/255, 251/255, 1),
        (55/255, 161/255, 251/255, 1),
        (55/255, 161/255, 251/255, 1),
        (1, 1, 0, 1)
    ])
    plt.scatter(tsne[:, 0], tsne[:, 1], marker='.', c=clusters,  cmap=color_map_available, s=100, label="Available Games")
    highlight_indices = np.where(clusters == 8)[0]
    plt.scatter(tsne[highlight_indices, 0], tsne[ highlight_indices, 1], color= 'hotpink', s=200, marker='x')
    plt.title('2 dimensional t-distance Stochastic Neighbour Embedding for Game Clusters using Compilation Concepts and Ludemes', fontsize=14, fontweight='bold')
    plt.grid(False)
    plt.xticks([])
    plt.yticks([])
    # plt.legend()
    plt.show()

def plot_2d_heatmap_tsne(tsne, regret_indices, regret_scores):
    plt.scatter(tsne[:, 0], tsne[:, 1], marker='.',  color=(55/255, 161/255, 251/255, 0.4), s=100)
    sc = plt.scatter(tsne[regret_indices, 0], tsne[regret_indices, 1], 
    c=regret_scores, cmap="RdYlGn_r", s=100, marker='o')
    cbar = plt.colorbar(sc, orientation='horizontal', fraction=0.046, pad=0.1)
    cbar.set_label("Regret")
    plt.savefig("plot.pdf", format="pdf", bbox_inches="tight")
    plt.show()

def plot_3d_tsne(tsne, labels, clusters, color_map='Selected', write=False):
    color_map_distinct = ['rgba(55, 161, 251, 1)', 'rgba(53, 179, 57, 1)', 'rgba(251, 53, 53, 1)', 'rgba(255, 126, 38, 1)','rgba(55, 161, 251, 1)', 'rgba(53, 179, 57, 1)', 'rgba(251, 53, 53, 1)', 'rgba(255, 126, 38, 1)', '#f505ff']
    color_map_available = ['rgba(0, 0, 0, 0.1)', 'rgba(0, 0, 0, 0.1)', 'rgba(0, 0, 0, 0.1)', 'rgba(0, 0, 0, 0.1)','rgba(55, 161, 251, 1)', 'rgba(53, 179, 57, 1)', 'rgba(251, 53, 53, 1)', 'rgba(255, 126, 38, 1)', '#f505ff']
    color_map_selected = ['rgba(0, 0, 0, 0)', 'rgba(0, 0, 0, 0)', 'rgba(0, 0, 0, 0)', 'rgba(0, 0, 0, 0)','rgba(0, 0, 0, 0.1)', 'rgba(0, 0, 0, 0.1)', 'rgba(0, 0, 0, 0.1)', 'rgba(0, 0, 0, 0.1)', '#f505ff']

    colormap = color_map_distinct if color_map == 'Full' else color_map_available if color_map == 'Available' else color_map_selected
    plot_data = pd.DataFrame(tsne, columns=['x', 'y', 'z'])
    fig = px.scatter_3d(
        plot_data, 
        x='x', y='y', z='z',
        color=clusters,
        color_continuous_scale=colormap, 
        hover_name=labels, 
    )
    fig.update_layout(
        scene=dict(
            xaxis=dict(
                visible=False
            ),
            yaxis=dict(
                visible=False
            ),
            zaxis=dict(
                visible=False
            ),
        ),
        coloraxis_showscale=False
    )

    if (write):
        fig.write_html('analysis/3d_tsne_plot.html', include_plotlyjs=True)
    else:
        fig.show()

def get_p_divergence(data):
    perplexity = np.arange(5, 55, 5)
    divergence = []
    for i in perplexity:
        tsne = TSNE(n_components=2, random_state=69, perplexity=i)
        tsne.fit_transform(data)
        divergence.append(tsne.kl_divergence_)

    return perplexity, divergence

def plot_divergence(perplexity, divergence):
    plt.figure()
    plt.plot(perplexity, divergence, marker='o')
    plt.title('Perlexity vs Divergence')
    plt.xlabel('Perplexity Values')
    plt.ylabel('Divergence')
    plt.xticks(perplexity)
    plt.grid(True)
    plt.show()