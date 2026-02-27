from typing import Callable

import pandas as _pd

A4_LANDSCAPE_INCHES = (11.69, 8.27)
A5_LANDSCAPE_INCHES = (8.27, 5.83)


def union_read_csv(*file_paths: str):
    return _pd.concat([_pd.read_csv(file) for file in file_paths])


def mean_ci_n(series: _pd.Series, alpha: float):
    """Calculate mean, CI half width, and number of elements in the given series"""
    from numpy import nan, sqrt
    from scipy import stats

    values = series.dropna().to_numpy(dtype=float)
    count = values.size
    if count == 0:
        return nan, nan, 0
    if count == 1:
        # Cannot determine CI use half with of zero
        return float(values.mean()), 0.0, 1

    mean = float(values.mean())
    # Sample standard deviation
    std_dev = float(values.std(ddof=1))
    t_val = float(stats.t.ppf(1 - alpha / 2, df=count - 1))
    half_width = t_val * std_dev / float(sqrt(count))
    return mean, half_width, count


def diff_mean_std_n_p(differences: _pd.Series):
    """Calculate mean, sample standard deviation, number of elements, and two-sided p-value for the given differences (after - before)"""
    from numpy import nan, sqrt
    from scipy import stats

    values = differences.dropna().to_numpy(dtype=float)
    count: int = len(values)
    if count == 0:
        return nan, nan, 0, nan

    ddof = 1

    mean = float(values.mean())
    std = float(values.std(ddof=ddof))
    # std_err = float(std / sqrt(float(count)))
    # t_stat = mean / std_err
    # df = count - ddof
    # p_two_sided = 2 * (1 - stats.t.cdf(abs(t_stat), df=df))
    # return mean, std, count, p_two_sided

    res = stats.ttest_1samp(differences.dropna(), popmean=0)
    return mean, std, count, res.pvalue


def aggregate_for_metric(
        data_frame: _pd.DataFrame,
        group_by: list[str],
        extract_metric: Callable[[_pd.DataFrame], _pd.Series],
        alpha: float,
):
    """Calculate mean & ci per (run, feedback type, iteration)"""
    rows = []
    for index, grouped in data_frame.groupby(group_by, sort=True):
        mean, hw, n = mean_ci_n(extract_metric(grouped), alpha)
        result = {
            'mean': mean,
            'hw': hw,
            'n': n,
        }
        for k, v in zip(group_by, index):
            result[k] = v
        rows.append(result)
    out = _pd.DataFrame(rows).sort_values(group_by)
    return out
