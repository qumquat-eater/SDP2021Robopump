# USAGE
# python Eclipse_detection.py --image image.png
# About the output:
# slant: the angle that the cap rotated that we see an eclipse instead of a circle
# Centroid: the center coordinate of the cap
# phi: the angle that the eclipse rotate without change of orientation



import matplotlib.pyplot as plt
import cv2
from skimage import color, img_as_ubyte
from skimage.feature import canny
from skimage.transform import hough_ellipse
from skimage.draw import ellipse_perimeter
import numpy as np
import argparse


# For calculate the diameter of the circle form the eclipse
def find_phi(xs,ys,cx,cy):
	max = 0
	max_X = 0
	max_Y = 0

	for i in range(len(xs)):
		distance = np.power(xs[i]-cx,2)+np.power(ys[i]-cy,2)
		if distance>max:
			max = distance
			max_X = xs[i]
			max_Y = ys[i]
	# print("inner_find_phi, the radian is: ", np.sqrt(np.power(max_X-cx,2)+np.power(max_Y-cy,2)))
	# print("max_Y,max_X=",max_Y,max_X)

	slant = np.arctanh((max_Y-cy)/(max_X-cx))
	return slant



def get_cap_info(image_rgb):
	# convert to grayscale and detect edges
	gray = cv2.cvtColor(image_rgb, cv2.COLOR_BGR2GRAY)
	thresh_for_bar = cv2.threshold(gray, 30, 255, cv2.THRESH_BINARY)[1]
	edges = canny(thresh_for_bar, sigma=2.0,
				  low_threshold=0.55, high_threshold=0.8)

	# cv2.imshow('tianbu',gray)
	# cv2.waitKey(0)

	# Perform a Hough Transform
	# The accuracy corresponds to the bin size of a major axis.
	# The value is chosen in order to get a single high accumulator.
	# The threshold eliminates low accumulators
	result = hough_ellipse(edges, accuracy=21, threshold=1,
						   min_size=1, max_size=None)
	# print('sorting start')
	result.sort(order='accumulator')
	# print('sorting finished')
	# Estimated parameters for the ellipse
	best = list(result[-1])
	yc, xc, a, b = [int(round(x)) for x in best[1:5]]
	orientation = best[5]

	slant = np.arccos(min(a,b)/max(a,b))
	print("slant = ",slant)

	print("Centroid = ","[",xc,",",yc,"]")
	# print("longest_radius=",a,"  shortest_radius=",b)


	cy, cx = ellipse_perimeter(yc, xc, a, b, orientation)

	# phi value of a rotated eclipse
	if np.abs(a-b)>2:
		phi = find_phi(cx,cy,xc,yc)
		print("phi = ",phi)
	else:
		print("phi = 0")
######
	# Draw the ellipse on the original image
	#image_rgb[cy, cx] = (0, 0, 255)
	# # Draw the edge (white) and the resulting ellipse (red)
	edges = color.gray2rgb(img_as_ubyte(edges))
	edges[cy, cx] = (250, 0, 0)

	fig2, (ax1, ax2) = plt.subplots(ncols=2, nrows=1, figsize=(8, 4),
									sharex=True, sharey=True)
	ax1.set_title('Original picture')
	ax1.imshow(image_rgb)
	ax2.set_title('Edge (white) and result (red)')
	ax2.imshow(edges)
	plt.show()


	return slant, phi, [xc,yc]
######

# construct the argument parse and parse the arguments
# ap = argparse.ArgumentParser()
# ap.add_argument("-i", "--image", required=True,
# 	help="path to the input image")
# args = vars(ap.parse_args())

# # load the image and resize it to a smaller factor so that
# # the shapes can be approximated better
# image = cv2.imread(args["image"])

# get_cap_info(image)