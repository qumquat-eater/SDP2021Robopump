import detect_shapes as ds
import custom_model_images as cm
import cv2
import argparse


def detect_fuel_cap(image_path):
    image_np, img_coords = cm.run(image_path)
    crop_img = image_np[img_coords[0]:img_coords[2], img_coords[1]:img_coords[3]]
    cv2.imwrite("output/cropped.png", crop_img)

    try:
        vertices, centroid, width, rotate_angle = ds.find_shape(crop_img)
        slant_angle = ds.find_slant_angle(crop_img)
        file = open("fuel_cap_coordinates.txt", "w")
        file.write("%s\n" % slant_angle)
        file.write("%s\n" % vertices)
        file.write("{} {}\n".format(centroid[0],centroid[1]))
        file.write("%s\n" % width)
        file.write("%s\n" % rotate_angle)
    except TypeError:
        print('Fuel cap has not been found')



ap = argparse.ArgumentParser()
ap.add_argument("-i", "--image", required=True,
    help="path to the input image")
args = vars(ap.parse_args())

detect_fuel_cap(args["image"])
