from numpy import *
import numpy as np
import matplotlib.pyplot as plt

__author__ = "Bhaarat Pachori"

# creating another matplotlib object.
plt1 = plt

"""
NOTE: Change the value of the below mentioned variables before running the program
      1.
        classification_report_path : path where classification report should get saved.
        Example:  '/Users/bhaarat/Desktop/classification.txt'
      2.
        data_file_path: absolute path to the data file.
        Example: data_file_path = '/Users/bhaarat/Documents/Fall 2017/BDA 720/Assignments/HW_02_1D_clustering/data_copy.csv'
"""

classification_report_path = '/Users/bhaarat/Desktop/classification.txt'
data_file_path = '/Users/bhaarat/Documents/Fall 2017/BDA 720/Assignments/HW_02_1D_clustering/data_copy.csv'

"""
This code works as follows:
1. The main function is called first.
2. Then method get_data_points() gets called, which read the data file and parse the data into local data structure.
3. Once, the data is parsed, the method, set_threshold() is called to create a data structure that holds all the possible
   values of the thresholds, starting from 0.05
4. Then we call the method named as "try_them_all()" which implements the pseudocode given in the lecture slides and
   helps in finding the best mixed variance value and best threshold value.
5. To confirm the above values return by the "try_them_all" method we do the same process but this time with the
   "Gradient Descent" algorithm. The method is "gradient_descent()"
6. When we compare the values of both the two methods, we have our two clusters named "ripe" and "unripe".
7. We then extend our algorithm to further make clusters from "ripe" cluster into "ripe and rotten" clusters. This is
   achieved by repeating the #4 and #5.
8. Once all these three clusters are formed we then plot them as shown in the writeup.
9. Finally, we plot a graph of Mixed variance as a function of threshold.
"""

"""
Description: This function opens and read the data points in the csv
             and stores them in a list named as points. Also, this
             method creates another list "points_threshold_coefficient"
             which contains the flexibility coefficient of the all the
             data points in the csv. The flexibility coefficient is
             calculated as "Deflection/Force".
"""
def get_data_points():
    """
    @:param: None
    :return: points_threshold_coefficient: value achieved by Deflection/Force
           : points: data points in the csv file.
    """
    points = genfromtxt(data_file_path, delimiter=',')
    points_threshold_coefficient = list()

    # skipping first row as it has the attribute names.
    for item in range(1, len(points)):
            coefficient = points[item][1] / points[item][0]
            points_threshold_coefficient.append(coefficient)
    return points_threshold_coefficient, points


"""
Description: This function takes the list of coefficient points
             (Deflection/Force) and finds the maximum value of the
             coefficient point, this max point seerves as the limit
             for the bins/step size. We start with bin size of 0.05
             and moves till the bin size is <= max value in the list.
             Here "threshold_limit" is the var that stores the max
            value.
"""
def set_threshold(points):
    """
    :param points: this is list of coefficient points, obtained as
                   Deflection/Force
    :return: threshold_list: list of all the threshold points
    """
    value = 0
    threshold_limit = max(points)
    threshold_list = []
    while value < threshold_limit:
        value += 0.05
        threshold_list.append(value)
    return threshold_list


"""
Description: This method implements the "TRY THEM ALL" method, which
             helps in finding the global minimum of the variance and
             the corresponding threshold. This method is a Brute Force method, where
             we can all the values of the coefficient_points(Deflection/Force) with the
             all the values in the "threshold" list. Each time we find a value that is
             smaller than the "best_mixed_variance" we save it, this helps us in finding
             the minimum value of mixed variance and threshold.
             This method internally calls another method named as "calculate_fraction_of_points"
             which basically finds the values which are > or <= threshold value.
"""
def try_them_all(coefficient_points, best_mixed_variance, best_threshold):
    """
    :param coefficient_points: Deflection/Force points.
    :param best_mixed_variance: holds the best variance value
    :param best_threshold: holds the best threshold value
    :param threshold_to_plot: list of bins/step/ threshold values
    :return: mixed_var_list: list of all the variacne found in the process
             under_thres : list of all the points that are less than or equal
                            to threshold we are testing for.
             over_thres : list of all the points that are above the threshold
                           we are testing for and for which iteration
             best_threshold: this contains the best threshold value found in the
                             execution of the method.
    """
    mixed_var_list = []
    under_thres = []
    over_thres = []
    for thres in threshold:
        under_var, wt_under, greater_var, wt_greater, under, over = calculate_fraction_of_points(thres, coefficient_points)
        mixed_variance = (wt_under * under_var) + (wt_greater * greater_var)
        mixed_var_list.append(mixed_variance)

        # Check to see if other best values happen
        if mixed_variance < best_mixed_variance:
            best_mixed_variance = mixed_variance
            best_threshold = thres
            under_thres = under
            over_thres = over

    print("Best Mixed Variance:",format(round(best_mixed_variance,2)))
    print("Best Threshold     :",format(round(best_threshold,2)))

    return mixed_var_list, under_thres, over_thres, best_threshold


"""
Description:
    This method is alternate and better approach as compared to "TRY THEM ALL"
    approach which was brute force. Here we calculate the slope of the line,
    the line which is formed by the points "mixed variance" and "thresholds".

    We check for the all the points for slope i.e. we find slope in both direction
    and move to that direction where the slope is smaller. If we find a slope that
    is smaller than the previous slope or the next slope we know that we have not found our
    global minimum yet and we proceed. This method runs for all the pair of
    points and keeps track of only the smallest slope found yet.
"""
def gradient_descent(grad_mixed_variance, grad_threshold):
    """
    :param grad_mixed_variance: list of mixed variance list.
    :param grad_threshold: list of all the threshold values.
    :return: None
    """
    best_slope = 10000.0
    x = 0
    y = 0
    slope_fwd = Inf
    slope_backward = Inf
    for item in range(len(threshold) -1):
        if item >= 0:
            slope_fwd = grad_threshold[item] - grad_threshold[item + 1] / grad_mixed_variance[item] - \
                             grad_mixed_variance[item + 1]
        if item > 0:
            slope_backward = grad_threshold[item ] - grad_threshold[item - 1] / grad_mixed_variance[item] - \
                        grad_mixed_variance[item - 1]
        min_slope = min(slope_backward, slope_fwd)
        if min_slope < best_slope:
            best_slope = min_slope
            x = grad_threshold[item]
            y = grad_mixed_variance[item]
    print("Best Variance :", format(round(y,2)))
    print("Best Threshold:", format(round(x,2)))


"""
Description: This method is used to calculate the fraction of points
             which are below or equal to or greater than the threshold value provided.
             This method is called repeatedly from its parent function named as a
             "try_them_all"
"""
def calculate_fraction_of_points(threshold_val, coefficient_points):
    """
    :param threshold_val:
    :param coefficient_points:
    :return: under_var: variance of points below or equal to the threshold.
             wt_under : list of points with weight
             greater_var: variance of points greater than threshold.
             under_or_equal_threshold : list to hold values <= threshold
             greater_not_equal_threshold : list to hold values > threshold
    """
    under_or_equal_threshold = list()
    greater_not_equal_threshold = list()
    under_var = 0
    greater_var = 0
    wt_greater = 0
    wt_under = 0
    # partition the data according to the threshold.
    for item in coefficient_points:
        if item <= threshold_val:
            under_or_equal_threshold.append(item)
        else:
            greater_not_equal_threshold.append(item)
    if len(under_or_equal_threshold) > 0:
        under_var = np.var(under_or_equal_threshold)
        wt_under = len(under_or_equal_threshold) / len(coefficient_points)

    if len(greater_not_equal_threshold) > 0:
        greater_var = np.var(greater_not_equal_threshold)
        wt_greater = len(greater_not_equal_threshold) / len(coefficient_points)

    return under_var, wt_under, greater_var, wt_greater, under_or_equal_threshold, greater_not_equal_threshold


"""
Description: This method is used to plot the clusters made out of the given data
    for the cantaloupes. This method plot three clusters of color black, green and
    red respectively.
    The Red represent : the riped cantaloupes
    The Green represent : the unriped cantaloupes
    The Black represent : the rotten cantaloupes
"""
def plot_clusters(best_threshold_val, new_best_threshold_val, plot, points_org):
    """
    :param best_threshold_val: hold the best threshold value found earlier.
    :param new_best_threshold_val: holds the new best threshold value for riped and
                                   rotten cantaloupes.
    :param plot: matplotlib object
    :param points_org: original points in the csv file.
    :return: None
    """
    ripe, unripe, ripe_x, ripe_y, unripe_x, unripe_y, rotten, rotten_x, rotten_y = []

    for item in range(1, len(points_org)):
        # checking unripe
        if points_org[item][1] / points_org[item][0] <= best_threshold_val:
            unripe.append(points_org[item])
            unripe_x.append(points_org[item][0])
            unripe_y.append(points_org[item][1])

        # Checking Ripe
        elif points_org[item][1] / points_org[item][0] > best_threshold_val and \
            points_org[item][1] / points_org[item][0] < new_best_threshold_val:
            ripe.append(points_org[item])
            ripe_x.append(points_org[item][0])
            ripe_y.append(points_org[item][1])

        # Checking Rotten
        else:
            rotten.append(points_org[item])
            rotten_x.append(points_org[item][0])
            rotten_y.append(points_org[item][1])

    plot.figure(1)
    plot.scatter(ripe_x, ripe_y, c="red", marker="*")
    plot.scatter(unripe_x, unripe_y, c="green", marker="o")
    plot.scatter(rotten_x, rotten_y, c="black", marker="+")
    plot.text(0.8, 17.5, "Cluster: Rotten", bbox=dict(facecolor='black', alpha=0.6))
    plot.text(0.8, 16.0, "Cluster: Riped", bbox=dict(facecolor='red', alpha=0.6))
    plot.text(0.8, 14.5, "Cluster: Not Riped", bbox=dict(facecolor='green', alpha=0.6))
    plot.show()


"""
Description: The method plots the final graph as the Mixed Variance as a
             function of threshold values.
"""
def plot_graph(graph_mixed_variance_list, graph_threshold, plot1):
    """
    :param graph_mixed_variance_list: list containing all the mixed variances
                                      plotted on (Y-axis)
    :param graph_threshold: list containing the threshold values (plotted on X- axis)
    :param plot1: matplotlib lib object
    :return: None
    """
    plot1.figure(2)
    plot1.plot(graph_threshold, graph_mixed_variance_list)
    plot1.ylabel("Mixed Variance")
    plot1.xlabel("Threshold")
    plot1.title('Mixed variance as a function of Threshold')
    plot1.show()


"""
Description:
            This method is used to write the classification report in a text format.
"""
def classification_report(class_best_threshold, class_next_best_threshold, class_points_org):
    """
    :param class_best_threshold: this value is 0.6
    :param class_next_best_threshold: this value is 0.9
    :param class_points_org: data file values of Force and Deflection.
    :return:
    """
    ripe = "Ripe"
    unripe = "Unripe"
    rotten = "Rotten"
    tab = "            "
    ripe_cnt = 0;
    unripe_cnt = 0
    rotten_cnt = 0
    class_points_org = class_points_org.tolist()
    file_obj = open(classification_report_path,"w+")
    file_obj.write("Force(N)            Deflection(mm)          Classification \n")
    for item in range(1, len(class_points_org)):
        if class_points_org[item][1] / class_points_org[item][0] <= class_best_threshold:
            buffer = str(class_points_org[item][0]) + tab + str(class_points_org[item][1]) + tab + unripe + "\n"
            unripe_cnt += 1
        elif class_points_org[item][1] / class_points_org[item][0] > class_best_threshold and \
                                class_points_org[item][1] / class_points_org[item][0] < class_next_best_threshold:
            buffer = str(class_points_org[item][0]) + tab + str(class_points_org[item][1]) + tab + ripe + "\n"
            ripe_cnt += 1
        else:
            buffer = str(class_points_org[item][0]) + tab + str(class_points_org[item][1]) + tab + rotten + "\n"
            rotten_cnt += 1
        file_obj.write(buffer)

    summary = "\nTotal Ripe:   " + str(ripe_cnt) + "\n" + "Total Unripe: " + str(unripe_cnt) + "\n" + "Total Rotten: " + str(rotten_cnt)
    file_obj.write(summary)

    file_obj.close()


if __name__ == '__main__':
    if __name__ == '__main__':
        # Setting up default values
        best_mixed_variance = 10000.0
        best_threshold = 0

        # This method returns the data points in the csv files
        # referred as "points_org"
        # It also return the flexibility coefficient of all the
        # data points too referred by "coeff_points"
        coeff_points, points_org = get_data_points()

        # Setting up the bins or step or threshold array
        # this is done by the method referred as "set_threshold"
        # it returns a list of all the possible threshold ranges
        # based on the step size. We have consider the step size
        # as 0.05
        threshold = set_threshold(coeff_points)

        # This is the try them all method which helps in finding the
        # minimum value for the mixed variance and corresponding threshold.
        print("*********** TRY THEM ALL (for clustering ripe and unripe) START ***********")
        mixed_variance_list, under, over, best_threshold = try_them_all(coeff_points, best_mixed_variance,
                                                                        best_threshold)
        print("*********** TRY THEM ALL (for clustering ripe and unripe) END  ************")
        print()

        # This method finds the global minimum for the variance
        # and corresponding threshold, this method is not brute
        # as "Try Them All"
        print("*********** GRADIENT DESCENT START ***********")
        gradient_descent(mixed_variance_list, threshold)
        print("*********** GRADIENT DESCENT END ***********")
        print()

        # Re-initializing the values for second use.
        next_best_mixed_variance = 10000.0
        next_best_threshold = 0

        # Finding the threshold to cluster the cantaloupes as
        # ripe or rotten.
        new_threshold = set_threshold(over)

        # Finding the global minimum for variance and threshold to
        # differentiate ripe and rotten cantaloupes.
        print("*********** TRY THEM ALL (for clustering ripe and rotten) START ***********")
        new_mixed_variance_list, under, over, new_best_threshold = try_them_all(over, next_best_mixed_variance,
                                                                                next_best_threshold)
        print("*********** TRY THEM ALL (for clustering ripe and rotten) END *************")

        # Plotting the final scatter plot to visualize how data is clustered.
        # Uncomment the below code to draw the clusters.

        # plot_clusters(best_threshold, new_best_threshold, plt, points_org)

        # Plotting mixed variance as a function of threshold value
        plot_graph(mixed_variance_list, threshold, plt1)

        # To write a text file, please comment the above line of code that plots the graph.
        # which is: plot_graph(mixed_variance_list, threshold, plt1)

        # create a classification report.
        classification_report(best_threshold, new_best_threshold, points_org)
