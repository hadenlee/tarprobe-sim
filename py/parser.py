from sys import stdin
import re
import numpy as np
import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt


def plots_by_n(df, title):
  # n_I = 150, Q = 75, Np = 2, TTP = 5
  d_both = (df[(df['n_I'] == 150)])[["prob", "N'", "type", "loss"]]

  print(d_both)

  sns.set_style("whitegrid")

  g = sns.catplot(x="prob", y="loss", hue="type", col="N'",
                  capsize=.2, palette="tab10", height=6, aspect=.75,
                  kind="point", data=d_both)#.set(title="n_I = 150  Q = 75  TTP = 5")

  plt.xlim(reversed(plt.xlim()))

  g.despine(left=True)

  g.savefig(title)

  # plt.show()
  


def plots_by_n_i(df, n_p, title):
  # n_I = 150, Q = 75, Np = 2, TTP = 5
  d_both = (df[(df["N'"] == n_p)])[["prob", "n_I", "type", "loss"]]

  print(d_both)

  sns.set_style("whitegrid")

  g = sns.catplot(x="prob", y="loss", hue="type", col="n_I",
                  capsize=.2, palette="tab10", height=6, aspect=.75,
                  kind="point", data=d_both)#.set(title="n_I = 150  Q = 75  TTP = 5")

  plt.xlim(reversed(plt.xlim()))

  g.despine(left=True)

  g.savefig(title)

  # plt.show()
  

data = []

with open('../data/2021-04-04-results.txt') as f:
  prev = []
  for line in f:
    line = line.rstrip()
    if line.find("[Hi]") == 0 or line.find("[Lo]") == 0:
      prev.append(line)
    elif line.find("===") == 0:
      # print(line)
      # prob, n_i, Q, N', TTP
      x = re.findall('\d*\.\d+|\d+', line)
      # print(x)
      h = re.findall('\d*\.\d+|\d+', prev[2])[2]
      l = re.findall('\d*\.\d+|\d+', prev[3])[2]
      # print(h, l)
      data.append([float(x[0]), int(x[1]), int(x[2]), int(x[3]), int(x[4]), "Hi", float(h)])
      data.append([float(x[0]), int(x[1]), int(x[2]), int(x[3]), int(x[4]), "Lo", float(l)])
      prev = []

df = pd.DataFrame(data, columns = ['prob', 'n_I', 'Q', 'N\'', 'TTP', 'type', 'loss'])

print(df)

plots_by_n(df, "Varying N' n_i=150.png")
plots_by_n_i(df, 2, "Varying n_i, N'=2.png")
plots_by_n_i(df, 3, "Varying n_i, N'=3.png")
plots_by_n_i(df, 4, "Varying n_i, N'=4.png")
plots_by_n_i(df, 5, "Varying n_i, N'=5.png")
