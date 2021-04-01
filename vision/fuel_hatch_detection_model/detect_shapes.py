 # USAGE
# python detect_shapes.py --image image.png

# 1st step
# Find the angle the camera need to rotate by calling method find_slant_angle()
# use newimage_down.png to test


# 2nd step
# Find the width of the bar and the angle of bar from horizontal by calling method find_shape()
# use newimage.png to test

# import the necessary packages
import numpy as np
import matplotlib.pyplot as plt
from pyimagesearch.shapedetector import ShapeDetector
import argparse
import imutils
import cv2



def img_pre_cap(img):
	# convert the resized image to grayscale, blur it slightly,
	# and threshold it
	gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
	blurred = cv2.GaussianBlur(gray, (5, 5), 0)
	thresh_for_cap = cv2.threshold(blurred, 30, 255, cv2.THRESH_BINARY_INV)[1]
	# thresh = cv2.threshold(blurred, 10, 255, cv2.THRESH_BINARY_INV)[1]
	# cv2.imshow("after threshold",thresh)
	# cv2.waitKey(500)
	return thresh_for_cap

def img_pre_bar(img):
	# convert the resized image to grayscale, blur it slightly,
	# and threshold it
	gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
	blurred = cv2.GaussianBlur(gray, (5, 5), 0)
	thresh_for_bar = cv2.threshold(blurred, 10, 255, cv2.THRESH_BINARY_INV)[1]
	# cv2.imshow("after threshold",thresh)
	# cv2.waitKey(500)
	return thresh_for_bar

def find_contour(img):
	# find contours in the thresholded image and initialize the
	# shape detector
	cnts = cv2.findContours(img.copy(), cv2.RETR_EXTERNAL,
		cv2.CHAIN_APPROX_SIMPLE)
	cnts = imutils.grab_contours(cnts)
	return cnts


def find_slant_angle(img):
	resized = imutils.resize(img, width=300)
	#cv2.imwrite("resized.jpg", resized)
	#cv2.waitKey(500)
	rat = img.shape[0] / float(resized.shape[0])
	sd = ShapeDetector()
	thresh_for_cap = img_pre_bar(resized)
	cnts_for_cap = find_contour(thresh_for_cap)
	for c in cnts_for_cap:
		M = cv2.moments(c)
		cX = int((M["m10"] / M["m00"]) * rat)
		cY = int((M["m01"] / M["m00"]) * rat)
		shape = sd.detect(c)
		Xmin = cX
		Xmax = cX
		Ymin = cY
		Ymax = cY
		for point in c:
			if point[0][0] < Xmin:
				Xmin = point[0][0]
			if point[0][1] < Ymin:
				Ymin = point[0][1]
			if point[0][0] > Xmax:
				Xmax = point[0][0]
			if point[0][1] > Ymax:
				Ymax = point[0][1]
		perimeter = Xmax - Xmin
		eclipse_peri = Ymax - Ymin

		if perimeter - eclipse_peri <= 2:
			slant_angle = 0
		else:
			slant_angle = np.arccos( (eclipse_peri-4)/perimeter)

		print("Slant Angle (rad):",slant_angle)
	return slant_angle

def find_shape(img):
	resized = imutils.resize(img, width=300)
	#cv2.imwrite("resized.jpg", resized)
	#cv2.waitKey(500)
	rat = img.shape[0] / float(resized.shape[0])
	sd = ShapeDetector()
	thresh_for_bar = img_pre_bar(resized)

	cnts_for_bar = find_contour(thresh_for_bar)

	# loop over the contours
	detect_circle = 0
	for c in cnts_for_bar:
		# compute the center of the contour, then detect the name of the
		# shape using only the contour
		M = cv2.moments(c)
		cX = int((M["m10"] / M["m00"]) * rat)
		cY = int((M["m01"] / M["m00"]) * rat)
		shape = sd.detect(c)
		Xmin = cX
		Xmax = cX
		Ymin = cY
		Ymax = cY
		Xmin_y,Xmax_y,Ymin_x,Ymax_x = 0,0,0,0
		for point in c:
			if point[0][0]	< Xmin:
				Xmin = point[0][0]
				Xmin_y = point[0][1]
			if point[0][1] < Ymin:
				Ymin = point[0][1]
				Ymin_x = point[0][0]
			if point[0][0] > Xmax:
				Xmax = point[0][0]
				Xmax_y = point[0][1]
			if point[0][1] > Ymax:
				Ymax = point[0][1]
				Ymax_x = point[0][0]
		# if slant_angle == 0:
		# 	left_high = True
		# 	if Xmin_y-Ymin < Xmax_y- Ymin
		# actualContour = [[[Xmin,Xmin_y]],[[Ymin_x,Ymin]],[[Xmax,Xmax_y]],[[Ymax_x,Ymax]]]
		actualContour = [[Xmin,Xmin_y],[Ymin_x,Ymin],[Xmax,Xmax_y],[Ymax_x,Ymax]]
		print("Vertice (pixel):",actualContour)
		print("Centroid (pixel):(",cX,",",cY,")")
		angle = np.arctan((Xmin_y-Ymin)/(Xmax-Ymax_x))
		Width1 = (Xmax_y - Ymin)/np.cos(angle)
		Width2 = (Xmax - Ymax_x)/np.cos(angle)
		if Width1>Width2:
			width = Width2
		else:
			width = Width1
		print("Width (pixel):", width)
		print("Rotate Angle (rad):",angle)
		# else:

			# Width = (Ymax-Ymin)*np.cos(angle)

		# multiply the contour (x, y)-coordinates by the resize ratio,
		# then draw the contours and the name of the shape on the image
		c = c.astype("float")
		c *= rat
		c = c.astype("int")
		cv2.drawContours(img, [c], -1, (0, 255,  ), 2)

		cv2.putText(img, shape, (cX, cY), cv2.FONT_HERSHEY_SIMPLEX, 
		2, (255, 255, 255), 2)

		# show the output image
		cv2.imwrite("output/fuel_cap.jpg", img)

		if shape == "circle":
			detect_circle = 1
			print("Found the cap!")
			return actualContour, (cX,cY), width, angle
	

		# try: 
		# 	if shape != "circle":
		# 		raise CapNotFoundError
		#     break
		# except CapNotFoundError:
		# 	print("This value is too small, try again!")

# # construct the argument parse and parse the arguments
# ap = argparse.ArgumentParser()
# ap.add_argument("-i", "--image", required=True,
# 	help="path to the input image")
# args = vars(ap.parse_args())

# # load the image and resize it to a smaller factor so that
# # the shapes can be approximated better
# image = cv2.imread(args["image"])

# find_slant_angle(image)
# find_shape(image)



